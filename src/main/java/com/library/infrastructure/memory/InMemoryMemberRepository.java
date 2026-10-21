package com.library.infrastructure.memory;

import com.library.domain.member.Member;
import com.library.domain.member.MemberRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class InMemoryMemberRepository implements MemberRepository {
    private final Map<Long, Member> storage = new HashMap<>();

    @Override public Optional<Member> findById(long id) { return Optional.ofNullable(storage.get(id)); }
    @Override public void save(Member member)           { storage.put(member.getId(), member); }
    @Override public List<Member> findAll()             {return List.copyOf(storage.values());}
}
