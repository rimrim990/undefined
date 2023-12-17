insert into company(name, region, series, created_at, last_modified_at)
    values('Google', 'SEOUL', 'A', now(), now());
insert into company(name, region, series, created_at, last_modified_at)
    values('Netflix', 'ETC', 'IPO', now(), now());
insert into company(name, region, series, created_at, last_modified_at)
    values('Amazon', 'SEOUL', 'D', now(), now());

insert into job(company_id, position, created_at, last_modified_at)
    select id, '백엔드', now(), now() from company;

insert into retrospect(content, good_point, bad_point, summary, score, created_at, last_modified_at)
    values('면접을 너무 잘 봤다', 'good', 'bad', '좋아', 5, now(), now());

insert into stage(job_id, name, state, created_at, last_modified_at)
    select id, '서류', 'PASS', now(), now() from job;
insert into stage(job_id, name, state, created_at, last_modified_at)
    select id, '코딩 테스트', 'PASS', now(), now() from job;
insert into stage(job_id, name, state, created_at, last_modified_at)
    select id, '1차 면접', 'WAIT', now(), now() from job;

update stage set retrospect_id = (select id from retrospect limit 1) limit 1;
