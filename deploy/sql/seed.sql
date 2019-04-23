--
-- seed.sql
--   SQL script to seed some useful data for the Artemis Service
--

--
-- Start with the superusers
--
insert into superusers (user_email)
  values ('bob.beaty@rate.com'),
         ('ilya.bernshteyn@rate.com'),
         ('jeffrey.sherman@rate.com');

--
-- Add in a few API tokens for fun
--
insert into api_tokens (token, app, email, owner, as_of)
  values('db8fa9f1-2272-49d9-897e-b25f7d21ec26', 'ogre', 'ogre@guaranteedrate.com', 'OGRE Team', current_timestamp),
        ('53dce879-5baf-456c-8653-86f4e4e6c514', 'bto', 'kirill.medvedev@guaranteedrate.com', 'Kirill Medvedev', current_timestamp),
        ('4a0819c9-eb60-4ff6-bae2-617d46149464', 'person', 'peter.tucker@guaranteedrate.com', 'P. Tucker', current_timestamp),
        ('47c7efe7-8087-47d3-b341-f3764d15920e', 'employee', 'peter.tucker@guaranteedrate.com', 'P. Tucker', current_timestamp),
        ('925d443d-2dbd-4555-be67-91c931188f62', 'gateless', 'grpteam@guaranteedrate.com', 'GRP Team', current_timestamp),
        ('692dfeef-d21e-416c-adb8-22c869a02cc0', 'gr-app', 'peter.tucker@guaranteedrate.com', 'PX Team', current_timestamp),
        ('47734067-6331-4163-8b6f-2162b4713f89', 'digitalmortgage', 'dmo-dev@guaranteedrate.com', 'DMO Team', current_timestamp),
        ('4e348bce-6f8e-4683-9de8-fb3bdd091b8c', 'bto-napalm', 'brian.begy@guaranteedrate.com', 'BTO Team', current_timestamp),
        ('5d3dc21e-6390-4255-8285-8fb7f888fc20', 'litebrite', 'polaris@guaranteedrate.com', 'Polaris Team', current_timestamp);

--
-- These are the initial LOs in the pilot - and that's all we need to start
-- this show with
--
insert into loan_officers(email, employee_id)
  values('ben@rate.com', 2800),
        ('drogers@rate.com', 2877),
        ('danielle.young@rate.com', 6531),
        ('debra.shultz@rate.com', 7023),
        ('dianne@rate.com', 12284),
        ('drew@rate.com', 486),
        ('hani@rate.com', 7347),
        ('indu.kapoor@rate.com', 12268),
        ('jd@rate.com', 287),
        ('jeff.crain@rate.com', 9650),
        ('beeston@rate.com', 8052),
        ('jennifer.fairfield@rate.com', 3191),
        ('jennifer.martinez@rate.com', 13544),
        ('joel.schaub@rate.com', 627),
        ('jnoldan@rate.com', 2869),
        ('larry@rate.com', 10697),
        ('maddox@rate.com', 11269),
        ('mparadis@rate.com', 2861),
        ('matt@rate.com', 291),
        ('michaelap@rate.com', 9393),
        ('michelle@rate.com', 2159),
        ('nicole.santizo@rate.com', 14001),
        ('oren.orkin@rate.com', 8864),
        ('rebecca.mott@rate.com', 9748),
        ('robert.bajakian@rate.com', 2382),
        ('ron.erdmann@rate.com', 9138),
        ('rmecum@rate.com', 10539),
        ('ssharp@rate.com', 2113),
        ('sammy@rate.com', 1996),
        ('savvas.fetfatsidis@rate.com', 1991),
        ('sean.knudsen@rate.com', 6514),
        ('shant@rate.com', 6068),
        ('sheri.arnold@rate.com', 14481),
        ('shimmy@rate.com', 222),
        ('todd.emerson.albrecht@rate.com', 12073),
        ('todd.martin@rate.com', 9847),
        ('tom.mcmurray@rate.com', 11103),
        ('tomlavallee@rate.com', 6789),
        ('allyson@rate.com', 6258),
        ('deanv@rate.com', 9185),
        ('brian.budd@rate.com', 8060),
        ('efrain.miranda@rate.com', 2801),
        ('michael.dill@rate.com', 11829),
        ('dan.gjeldum@rate.com', 3443);
