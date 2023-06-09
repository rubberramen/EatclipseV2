package com.eatclipseV2.entity;

import com.eatclipseV2.domain.member.dto.MemberEditFormDto;
import com.eatclipseV2.domain.member.dto.MemberFormDto;
import com.eatclipseV2.entity.enums.Role;
import com.eatclipseV2.exception.NotEnoughCashException;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "members")
@Getter
@Setter
@ToString
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "member_id")
    private Long id;

    private String name;

    private String nickName;

    private String password;

    @Embedded
    private Address address;

    @Enumerated(EnumType.STRING)
    private Role role;

    private int cash;

//    @OneToMany
//    private List<Order> orders;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Order> orders = new ArrayList<>();

    @OneToOne(mappedBy = "member")   // cascade = CascadeType.ALL, orphanRemoval = true
    private Cart cart;

    public static Member createMember(MemberFormDto memberFormDto) {

        Member member = new Member();
        member.setName(memberFormDto.getName());
        member.setNickName(memberFormDto.getNickName());
        member.setPassword(memberFormDto.getPassword());
        member.setAddress(new Address(memberFormDto.getCity(), memberFormDto.getStreet(), memberFormDto.getZipcode()));
        member.setRole(Role.USER);
        member.setCash(0);

        return member;
    }

    public static MemberEditFormDto createEditForm(Member member) {
        MemberEditFormDto dto = new MemberEditFormDto();
        dto.setId(member.getId());
        dto.setName(member.getName());
        dto.setNickName(member.getNickName());
        dto.setCity(member.getAddress().getCity());
        dto.setStreet(member.getAddress().getStreet());
        dto.setZipcode(member.getAddress().getZipcode());
        return dto;
    }

    public Member() {
    }

    public Member(String name, String nickName, String password, Address address, Role role, int cash) {
        this.name = name;
        this.nickName = nickName;
        this.password = password;
        this.address = address;
        this.role = role;
        this.cash = cash;
    }

    public void minusCash(int amount) {

        int tmpCash = cash - amount;

        if (tmpCash < 0) {
            throw new NotEnoughCashException("캐시가 부족합니다. (현재 캐시 : " + cash +
                    ", 주문 총 금액 : " + amount + ")");
        }

        cash -= amount;
    }
}
