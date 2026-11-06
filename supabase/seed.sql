-- Anonymous synthetic identities can be impersonated by the local harness.
insert into auth.users (id, aud, role, is_anonymous, created_at, updated_at)
values
  ('10000000-0000-4000-8000-000000000001', 'authenticated', 'authenticated', true, now(), now()),
  ('10000000-0000-4000-8000-000000000002', 'authenticated', 'authenticated', true, now(), now()),
  ('10000000-0000-4000-8000-000000000003', 'authenticated', 'authenticated', true, now(), now());

insert into public.boards (id, name, created_by)
values
  ('20000000-0000-4000-8000-000000000001', 'North board', '10000000-0000-4000-8000-000000000001'),
  ('20000000-0000-4000-8000-000000000002', 'South board', '10000000-0000-4000-8000-000000000003');

insert into public.board_members (board_id, user_id, role)
values ('20000000-0000-4000-8000-000000000001', '10000000-0000-4000-8000-000000000002', 'member');

insert into public.tasks (id, board_id, title, status, priority, completed, created_at, completed_at)
values
  ('30000000-0000-4000-8000-000000000001', '20000000-0000-4000-8000-000000000001', 'Sort backlog', 'todo', 'low', false, now(), null),
  ('30000000-0000-4000-8000-000000000002', '20000000-0000-4000-8000-000000000001', 'Archive old item', 'done', 'low', true, now() - interval '45 days', now() - interval '40 days'),
  ('30000000-0000-4000-8000-000000000003', '20000000-0000-4000-8000-000000000002', 'Review checklist', 'doing', 'high', false, now(), null);
