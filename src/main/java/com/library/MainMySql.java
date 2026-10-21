package com.library;

import com.library.application.LibraryService;
import com.library.domain.book.BookRepository;
import com.library.domain.event.EventPublisher;
import com.library.domain.fee.StandardFeePolicy;
import com.library.domain.member.MemberRepository;
import com.library.infrastructure.jdbc.JdbcBookRepository;
import com.library.infrastructure.jdbc.JdbcMemberRepository;
import com.library.infrastructure.jdbc.SimpleDataSource;
import com.library.infrastructure.jdbc.dao.BookDao;
import com.library.infrastructure.jdbc.dao.LoanDao;
import com.library.infrastructure.jdbc.dao.MemberDao;
import com.library.infrastructure.proxy.TimingProxy;
import com.library.ui.ConsoleApp;

import java.time.Clock;


public class MainMySql {
    public static void main(String[] args) {
        try (var app = LibraryApp.withMySql()) {
            new ConsoleApp(app.service(), app.circulation()).run();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
