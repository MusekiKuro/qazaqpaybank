package kz.qazaqpay.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kz.qazaqpay.model.dto.request.CardCreateRequest;
import kz.qazaqpay.model.dto.response.CardResponse;
import kz.qazaqpay.model.entity.User;
import kz.qazaqpay.service.CardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Card Management", description = "Endpoints for issuing and managing cards")
public class CardController {

    private final CardService cardService;

    @PostMapping("/create")
    @Operation(summary = "Issue a new card", description = "Create a new debit card linked to an account")
    public ResponseEntity<CardResponse> createCard(@Valid @RequestBody CardCreateRequest request,
                                                   @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(cardService.createCard(request, user));
    }

    @GetMapping("/my-cards")
    @Operation(summary = "Get my cards", description = "List all cards belonging to the user")
    public ResponseEntity<List<CardResponse>> getMyCards(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(cardService.getMyCards(user));
    }

    @PostMapping("/{cardId}/toggle-block")
    @Operation(summary = "Block/Unblock card", description = "Toggle card status between ACTIVE and BLOCKED")
    public ResponseEntity<CardResponse> toggleBlock(@PathVariable Long cardId,
                                                    @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(cardService.toggleBlock(cardId, user));
    }
}