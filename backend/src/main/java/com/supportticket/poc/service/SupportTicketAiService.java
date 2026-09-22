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

        String prompt = """
                You are a support ticket assistant.

                Answer the user's question using ONLY the support ticket
                information provided in the context below.

                Do not invent ticket information.
                If the requested information is not present in the context,
                explicitly say that it is not available.

                CONTEXT:
                %s

                USER QUESTION:
                %s
                """.formatted(context, question);

        String answer = chatClient.prompt()
                .user(prompt)
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
