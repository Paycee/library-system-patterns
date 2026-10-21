package com.library.infrastructure.jdbc;

import com.library.domain.member.Member;
import com.library.domain.member.MemberRepository;
import com.library.infrastructure.jdbc.dao.MemberDao;
import com.library.infrastructure.jdbc.dao.MemberRow;

import java.util.List;
import java.util.Optional;

public class JdbcMemberRepository implements MemberRepository {
    private final MemberDao memberDao;

    public JdbcMemberRepository(MemberDao memberDao) {
        this.memberDao = memberDao;
    }

    @Override
    public Optional<Member> findById(long id) {

        return memberDao.selectById(id).map(r -> new Member(r.id(), r.name()));
    }

    @Override
    public void save(Member member) {
        memberDao.upsert(new MemberRow(member.getId(), member.getName()));
    }

    @Override
    public List<Member> findAll() {
        return memberDao.selectAll().stream().map(r -> new Member(r.id(), r.name())).toList();
    }
}
