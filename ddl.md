## 数据库地址
- 数据库地址：blueing.fun:3306
- 用户名：root

## 原DDL（如果有人玩坏了数据库，这是备份脚本）

```sql
create table sports_meet.demo_sm
(
    sm_id         int auto_increment
        primary key,
    name          varchar(24)  null,
    sport_count   int          null,
    img           varchar(256) null,
    status        int          null,
    number_config text         null,
    max_players   int          null,
    max_events    int          null,
    abstract      text         null,
    am_start_time time         null,
    pm_start_time time         null,
    am_end_time   time         null,
    pm_end_time   time         null
);

create table sports_meet.judge_rules
(
    rid          int auto_increment
        primary key,
    name         varchar(32) null,
    units        char(32)    null,
    mappings     text        null,
    sm_id        int         null,
    is_rank_mode tinyint(1)  null
);

create table sports_meet.judges
(
    jid   int null,
    sp_id int null,
    uid   int null
);

create index sp_id
    on sports_meet.judges (sp_id);

create table sports_meet.schools
(
    sc_id        int auto_increment
        primary key,
    name         varchar(24)  null,
    player_count int          null,
    app_count    int          null,
    slogan       varchar(64)  null,
    img          varchar(256) null,
    sm_id        int          null,
    type         int          null,
    a_rank       int          null,
    degree       int          null,
    team_number  text         null
);

create table sports_meet.sm
(
    sm_id                 int auto_increment
        primary key,
    name                  varchar(24)  null,
    sport_count           int          null,
    school_count          int          null,
    player_count          int          null,
    img                   varchar(256) null,
    status                int          null,
    start_time            datetime     null,
    end_time              datetime     null,
    am_start_time         time         null,
    am_end_time           time         null,
    pm_start_time         time         null,
    pm_end_time           time         null,
    number_config         text         null,
    max_players           int          null,
    max_events            int          null,
    registration_deadline datetime     null,
    abstract              text         null
);

create table sports_meet.invite_code
(
    code         varchar(24) null,
    power_degree varchar(24) null,
    expire_time  datetime    null,
    iid          int auto_increment
        primary key,
    sm_id        int         null,
    user         varchar(32) null,
    constraint code
        unique (code),
    constraint user
        unique (user),
    constraint invite_code_ibfk_1
        foreign key (sm_id) references sports_meet.sm (sm_id)
);

create index sm_id
    on sports_meet.invite_code (sm_id);

create table sports_meet.sp_groups
(
    gid   int auto_increment
        primary key,
    name  char(24) null,
    sm_id int      null,
    sp_id int      null
);

create table sports_meet.sports
(
    sp_id            int auto_increment
        primary key,
    name             varchar(24)   null,
    app_start_time   datetime      null,
    app_end_time     datetime      null,
    player_count     int default 0 null,
    game_start_time  datetime      null,
    game_end_time    datetime      null,
    judge_start_time datetime      null,
    judge_end_time   datetime      null,
    comp_system      int           null,
    comp_type        int           null,
    main_sp_id       int           null,
    venue            char(24)      null,
    sm_id            int           null,
    event_type       int           null,
    sub_event_type   int           null,
    size             char(16)      null,
    count_pgp        int           null,
    status           int           null,
    rise_count       int           null,
    rise_type        tinyint(1)    null,
    gender           tinyint(1)    null,
    rid              int           null
);

create table sports_meet.user_sm
(
    uid            int         null,
    sm_id          int         null,
    power_degree   varchar(32) null,
    joined_schools varchar(48) null
);

create table sports_meet.users
(
    uid          int auto_increment
        primary key,
    name         varchar(12)  null,
    password     varchar(24)  null,
    head         varchar(256) null,
    sc_id        int          null,
    sm_id        int          null,
    joined_sms   varchar(32)  null,
    judge_events varchar(32)  null,
    constraint name
        unique (name)
);

create table sports_meet.players
(
    pid          int auto_increment
        primary key,
    age          int         null,
    gender       tinyint(1)  null,
    p_class      varchar(24) null,
    uid          int         null,
    sc_id        int         null,
    number       varchar(24) null,
    sports       varchar(48) null,
    name         varchar(48) null comment '名字',
    t_rank       int         null,
    sm_rank      int         null,
    total_degree double      null,
    sm_id        int         null,
    user_type    int         null,
    constraint players_ibfk_4
        foreign key (uid) references sports_meet.users (uid)
            on update cascade on delete set null
);

create table sports_meet.application_sports
(
    aid      int auto_increment
        primary key,
    pid      int      null,
    sp_id    int      null,
    app_time datetime null,
    score    char(24) null,
    degree   double   null,
    a_rank   int      null,
    gid      int      null,
    sm_id    int      null,
    status   int      null,
    constraint application_sports_ibfk_1
        foreign key (pid) references sports_meet.players (pid)
            on update cascade on delete set NULL
);

create index gid
    on sports_meet.application_sports (gid);

create index pid
    on sports_meet.application_sports (pid);

create index sp_id
    on sports_meet.application_sports (sp_id);

create index sc_id
    on sports_meet.players (sc_id);

create index uid
    on sports_meet.players (uid);

create index sc_id
    on sports_meet.users (sc_id);


```