--
-- create_all.sql
--   SQL script to make everything for the Newsroom Service application in
--   the database
--

--
-- Make sure that we have the UUID functions available in this database
--
create extension if not exists "uuid-ossp";

--
-- this is the table for the superusers of this app
--
create table if not exists superusers (
  user_email      varchar not null,
  primary key (user_email)
);

--
-- open up the table for everyone to read
--
grant select on superusers to public;


--
-- this is the table for the admins of this app
--
create table if not exists admins (
  user_email      varchar not null,
  primary key (user_email)
);

--
-- open up the table for everyone to read
--
grant select on admins to public;


--
-- this is the table for the persistent security tokens for the service
-- they can be added and removed easily, and allow external apps to have
-- access to the system.
--
create table if not exists api_tokens (
  token           uuid not null,
  app             varchar not null,
  email           varchar not null,
  owner           varchar not null,
  as_of           timestamp with time zone not null,
  primary key (token)
);

--
-- create some decent indexes for the recalling
--
create index idx_apit_app on api_tokens (app);
create index idx_apit_em on api_tokens (email);
create index idx_apit_own on api_tokens (owner);
create index idx_apit_aof on api_tokens (as_of);

--
-- open up the table for everyone to read
--
grant select on api_tokens to public;


--
-- create some functions to make the indexing of dates and datetimes easier
--
create or replace function mk_date(src varchar)
returns date
immutable as $body$
  select src::date;
$body$ language sql;

create or replace function mk_datetime(src varchar)
returns timestamp
immutable as $body$
  select src::timestamp;
$body$ language sql;

create or replace function mk_integer(src varchar)
returns integer
immutable as $body$
  select src::integer;
$body$ language sql;

create or replace function mk_double(src varchar)
returns double precision
immutable as $body$
  select src::double precision;
$body$ language sql;

create or replace function mk_bool(src varchar)
returns boolean
immutable as $body$
  select src::boolean;
$body$ language sql;


--
-- Table of all the LOs that we should be sending data to Total Expert for
--
create table if not exists loan_officers (
  employee_id     integer not null,
  email           varchar not null,
  active          boolean not null default true,
  as_of           timestamp with time zone not null default current_timestamp,
  primary key (employee_id)
);

--
-- create some decent indexes for the recalling
--
create index idx_los_em on loan_officers (email);
create index idx_los_act on loan_officers (active);
create index idx_los_aof on loan_officers (as_of);

--
-- open up the table for everyone to read
--
grant select on loan_officers to public;


--
-- Table of all the Contacts as they sit in Total Expert. This matches their
-- data model so that explains why it looks like it does.
--
create table if not exists te_contacts (
  address                       varchar,
  address_2                     varchar,
  birthday                      timestamp with time zone,
  city                          varchar,
  classification                varchar,
  close_date                    timestamp with time zone,
  contact_groups                jsonb,
  creation_date                 timestamp with time zone,
  credit_score                  varchar,
  credit_score_date             timestamp with time zone,
  credit_score_expiration_date  timestamp with time zone,
  email                         varchar,
  email_work                    varchar,
  employer_address              varchar,
  employer_address_2            varchar,
  employer_city                 varchar,
  employer_license_number       varchar,
  employer_name                 varchar,
  employer_state                varchar,
  employer_zip                  varchar,
  external_id                   varchar,
  external_ids                  jsonb,
  first_name                    varchar,
  id                            integer not null,
  internal_created_at           timestamp with time zone,
  internal_updated_at           timestamp with time zone,
  last_contacted_date           timestamp with time zone,
  last_modified_date            timestamp with time zone,
  last_name                     varchar,
  license_number                varchar,
  list_date                     timestamp with time zone,
  ok_to_call                    boolean,
  ok_to_email                   boolean,
  ok_to_mail                    boolean,
  owner_email                   varchar,
  owner_external_id             varchar,
  owner_id                      integer,
  owner_username                varchar,
  phone_cell                    varchar,
  phone_home                    varchar,
  phone_office                  varchar,
  referred_by                   varchar,
  referred_to                   varchar,
  source                        varchar,
  state                         varchar,
  suffix                        varchar,
  title                         varchar,
  zip_code                      varchar,
  as_of                         timestamp with time zone not null default current_timestamp,
  primary key (id)
);

--
-- create some decent indexes for the recalling
--
create index idx_tec_cl on te_contacts (classification);
create index idx_tec_cgrp on te_contacts using gin (contact_groups);
create index idx_tec_em on te_contacts (email);
create index idx_tec_xid on te_contacts (external_id);
create index idx_tec_xids on te_contacts using gin (external_ids);
create index idx_tec_oid on te_contacts (owner_id);
create index idx_tec_oxid on te_contacts (owner_external_id);
create index idx_tec_oun on te_contacts (owner_username);
create index idx_tec_oem on te_contacts (owner_email);
create index idx_tec_src on te_contacts (source);
create index idx_tec_fnm on te_contacts (first_name);
create index idx_tec_lnm on te_contacts (last_name);
create index idx_tec_aof on te_contacts (as_of);

--
-- open up the table for everyone to read
--
grant select on te_contacts to public;
