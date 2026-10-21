package com.library.domain.member;

import java.util.List;
import java.util.Optional;

public interface MemberRepository {
    Optional<Member> findById(long id);
    void save(Member member);
    List<Member> findAll();
}
