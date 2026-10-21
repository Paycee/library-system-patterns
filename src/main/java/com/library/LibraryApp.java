package com.library;

import com.library.application.LibraryService;
import com.library.application.circulation.*;
import com.library.config.LibraryConfig;
import com.library.domain.book.Book;
import com.library.domain.book.BookRepository;
import com.library.domain.book.Genre;
import com.library.domain.event.ActivityLogListener;
import com.library.domain.event.EventPublisher;
import com.library.domain.event.StatisticsListener;
import com.library.domain.fee.LateFeePolicy;
import com.library.domain.fee.MaxFeeDecorator;
import com.library.domain.fee.StandardFeePolicy;
import com.library.domain.member.Member;
import com.library.domain.member.MemberRepository;
import com.library.infrastructure.jdbc.JdbcBookRepository;
import com.library.infrastructure.jdbc.JdbcMemberRepository;
import com.library.infrastructure.jdbc.SimpleDataSource;
import com.library.infrastructure.jdbc.dao.BookDao;
import com.library.infrastructure.jdbc.dao.LoanDao;
import com.library.infrastructure.jdbc.dao.MemberDao;
import com.library.infrastructure.memory.InMemoryBookRepository;
import com.library.infrastructure.memory.InMemoryMemberRepository;
import com.library.infrastructure.mongo.MongoBookRepository;
import com.library.infrastructure.notification.ConsoleNotifier;
import com.library.infrastructure.proxy.LazyBookRepository;
import com.library.infrastructure.proxy.TimingProxy;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

import java.time.Clock;

public class LibraryApp implements AutoCloseable{

    private final LibraryService service;
    private final StatisticsListener statistics;
    private final AutoCloseable resource;
    private final CirculationMediator circulation;

    private LibraryApp(LibraryService service, StatisticsListener statistics, AutoCloseable resource, CirculationMediator circulation)
    {
        this.service = service;
        this.statistics = statistics;
        this.resource = resource;
        this.circulation = circulation;
    }

    public static LibraryApp withMySql()
    {
        LibraryConfig config = LibraryConfig.getInstance();
        var dataSource = mysql(config);
        BookRepository books = TimingProxy.wrap(BookRepository.class,
                new JdbcBookRepository(dataSource, new BookDao(dataSource), new LoanDao(dataSource)));

        return assemble(books, members(dataSource), config, () -> { });
    }

    public static LibraryApp withMongo()
    {
        LibraryConfig config = LibraryConfig.getInstance();
        MongoClient client = MongoClients.create(config.mongoUri());
        BookRepository books = TimingProxy.wrap(BookRepository.class,
                new LazyBookRepository(() -> new MongoBookRepository(client.getDatabase("biblioteka"))));

        return assemble(books, members(mysql(config)), config, client);
    }

    public static LibraryApp inMemory()
    {
        var books = new InMemoryBookRepository();
        books.save(Book.builder().id(1).title("Na Drini ćuprija").author("Ivo Andrić")
                .pages(380).genre(Genre.ROMAN).shelfId(1).build());
        books.save(Book.builder().id(2).title("Prokleta avlija").author("Ivo Andrić")
                .pages(150).genre(Genre.ROMAN).shelfId(1).build());
        books.save(Book.builder().id(3).title("Ubistvo u Orijent ekspresu").author("Agata Kristi")
                .pages(260).genre(Genre.KRIMI).shelfId(2).build());
        books.save(Book.builder().id(4).title("Deset malih crnaca").author("Agata Kristi")
                .pages(240).genre(Genre.KRIMI).shelfId(2).build());

        var members = new InMemoryMemberRepository();
        members.save(new Member(1, "Pera"));
        members.save(new Member(2, "Mika"));

        return assemble(books, members, LibraryConfig.getInstance(), () -> { });
    }


    // ------------ ZAJEDNICKE ZA SVE ----------------
    private static LibraryApp assemble(BookRepository books, MemberRepository members, LibraryConfig config, AutoCloseable resource)
    {
        LateFeePolicy feePolicy = new MaxFeeDecorator(new StandardFeePolicy(), config.maxFee());

        var circulation = new LibraryCirculation(books, new WaitingList(), new Cashier(), new ConsoleNotifier());

        var events = new EventPublisher();
        events.subscribe(new ActivityLogListener());
        var statistics = new StatisticsListener();
        events.subscribe(statistics);
        events.subscribe(circulation);

        var service = new LibraryService(books, members, Clock.systemDefaultZone(), feePolicy, events);
        return new LibraryApp(service, statistics, resource, circulation);
    }

    private static SimpleDataSource mysql(LibraryConfig config)
    {
        return new SimpleDataSource(config.mysqlUrl(), config.mysqlUser(), config.mysqlPassword());
    }

    private static MemberRepository members(SimpleDataSource dataSource)
    {
        return TimingProxy.wrap(MemberRepository.class, new JdbcMemberRepository(new MemberDao(dataSource)));
    }

    public LibraryService service() {return service;}
    public StatisticsListener statistics() {return statistics;}
    public CirculationMediator circulation() {return circulation;}

    @Override
    public void close() throws Exception {
        resource.close();
    }
}
