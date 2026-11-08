begin;

do $$
declare
  task_id uuid;
  finished_at timestamptz;
begin
  insert into public.tasks (board_id, title, completed)
  values ('20000000-0000-4000-8000-000000000001', 'Lifecycle test', true)
  returning id, completed_at into task_id, finished_at;
  assert finished_at is not null, 'completed insert requires a timestamp';

  update public.tasks set title = 'Updated' where id = task_id;
  assert (select completed_at = finished_at from public.tasks where id = task_id), 'unrelated updates must preserve timestamp';
  update public.tasks set completed = false where id = task_id;
  assert (select completed_at is null from public.tasks where id = task_id), 'reopening must clear timestamp';
  update public.tasks set completed = true where id = task_id;
  assert (select completed_at is not null from public.tasks where id = task_id), 'completion must set timestamp';

  begin
    update public.tasks set status = 'invalid' where id = task_id;
    raise exception 'invalid status accepted';
  exception when check_violation then null;
  end;
  begin
    update public.tasks set priority = 'invalid' where id = task_id;
    raise exception 'invalid priority accepted';
  exception when check_violation then null;
  end;
end;
$$;
rollback;
