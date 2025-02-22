package com.cyna.auth_users.users.service;

import com.cyna.auth_users.users.dto.CardDto;
import com.cyna.auth_users.users.models.BankCard;
import com.cyna.auth_users.users.models.User;
import com.cyna.auth_users.users.repositories.CardRepository;
import com.cyna.auth_users.users.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CardService {
    private final CardRepository cardRepository;
     private final UserRepository userRepository;

    public List<BankCard> getAll() {
        return cardRepository.findAll();
    }

    public BankCard get(Long id) {
        return cardRepository.findById(id).orElse(null);
    }

    public String create(CardDto card) {
        User user = userRepository.findById(card.getUser_id()).orElseThrow();
        BankCard bankCard = BankCard.builder()
                .user(user)
                .ownerName(card.getOwner())
                .number(card.getNumber())
                .expirationDate(card.getExpirationDate())
                .build();

        cardRepository.save(bankCard);
        return "Operation successful";
    }
    public String update(Long id, CardDto card) {
        BankCard bankCard = cardRepository.getReferenceById(id);

        BankCard updatedCard = BankCard.builder()
                .id(id)
                .ownerName(Optional.ofNullable(card.getOwner()).orElse(bankCard.getOwnerName()))
                .number(Optional.ofNullable(card.getNumber()).orElse(bankCard.getNumber()))
                .expirationDate(Optional.ofNullable(card.getExpirationDate()).orElse(bankCard.getExpirationDate()))
                .build();

        cardRepository.save(updatedCard);
        return "Operation successful";
    }

    public String delete(Long id) {
        cardRepository.deleteById(id);
        return "Operation successful";
    }
}
