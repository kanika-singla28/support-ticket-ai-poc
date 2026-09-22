package com.supportticket.poc.service;

import com.supportticket.poc.dto.AskAiResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class SupportTicketAiService {

    private final PgVectorStore pgVectorStore;
    private final ChatClient chatClient;
    private final int topK;
    private final double similarityThreshold;

    public SupportTicketAiService(
            PgVectorStore pgVectorStore,
            ChatModel chatModel,
            @Value("${poc.rag.top-k:5}") int topK,
            @Value("${poc.rag.similarity-threshold:0.75}") double similarityThreshold) {

        this.pgVectorStore = pgVectorStore;
        this.chatClient = ChatClient.builder(chatModel).build();
        this.topK = topK;
        this.similarityThreshold = similarityThreshold;
    }

    public AskAiResponse ask(String question) {

        SearchRequest searchRequest = SearchRequest.builder()
                .query(question)
                .topK(topK)
                .similarityThreshold(similarityThreshold)
                .build();

        List<Document> documents = pgVectorStore.similaritySearch(searchRequest);

        if (documents == null || documents.isEmpty()) {
            return new AskAiResponse(
                    "I could not find a relevant support ticket for this question.",
                    false,
                    List.of()
            );
        }

        System.out.println("===== RAG RETRIEVAL DEBUG =====");
        System.out.println("Question: " + question);
        System.out.println("Retrieved documents: " + documents.size());

        for (Document document : documents) {
            System.out.println("--------------------------------");
            System.out.println("Document ID: " + document.getId());
            System.out.println("Score: " + document.getScore());
            System.out.println("Metadata: " + document.getMetadata());
            System.out.println("Content:");
            System.out.println(document.getText());
        }

        String context = documents.stream()
                .map(Document::getText)
                .filter(Objects::nonNull)
                .reduce("", (left, right) -> left + "\n\n" + right);

        System.out.println("===== FINAL CONTEXT SENT TO MODEL =====");
        System.out.println(context);
        System.out.println("=======================================");

        String systemPrompt = """
                You are a support-ticket question-answering assistant.

                STRICT GROUNDING RULES:
                - Answer ONLY from the SUPPORT TICKET CONTEXT supplied by the application.
                - Treat Title, Description, Priority, Status, Category, Assignee,
                  Resolution and Comments as authoritative ticket information.
                - Read the Comments section carefully. Comments are valid evidence.
                - Read the Resolution section carefully. Resolution text is valid evidence.
                - If the answer is explicitly present in a comment, use that information.
                - If the answer is explicitly present in the resolution, use that information.
                - Do not claim information is missing when it appears in the supplied context.
                - Do not invent facts, causes, resolutions, comments or ticket IDs.
                - Answer the specific question directly and concisely.
                - Do not repeat unrelated ticket fields unless needed for the answer.
                - If the supplied context genuinely does not contain enough information,
                  say: "The retrieved ticket information does not contain enough information to answer this question."
                """;

        String userPrompt = """
                SUPPORT TICKET CONTEXT
                ======================
                %s
                ======================
                END SUPPORT TICKET CONTEXT

                QUESTION:
                %s
                """.formatted(context, question);

        String answer = chatClient.prompt()
                .system(systemPrompt)
                .user(userPrompt)
                .call()
                .content();

        List<AskAiResponse.Source> sources = documents.stream()
                .map(document -> document.getMetadata().get("ticketId"))
                .filter(Objects::nonNull)
                .map(Object::toString)
                .map(Long::valueOf)
                .distinct()
                .map(AskAiResponse.Source::new)
                .toList();

        return new AskAiResponse(
                answer,
                true,
                sources
        );
    }
}
