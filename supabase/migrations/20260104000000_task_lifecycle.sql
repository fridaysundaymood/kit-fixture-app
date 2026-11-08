alter table public.tasks
  add constraint tasks_status_check check (status in ('todo', 'doing', 'done')),
  add constraint tasks_priority_check check (priority in ('low', 'high'));

create function public.set_task_completed_at()
returns trigger language plpgsql set search_path = ''
as $$
begin
  if tg_op = 'INSERT' then
    if new.completed then
      new.completed_at := coalesce(new.completed_at, now());
    else
      new.completed_at := null;
    end if;
  elsif new.completed is distinct from old.completed then
    new.completed_at := case when new.completed then now() else null end;
  else
    new.completed_at := old.completed_at;
  end if;
  return new;
end;
$$;
revoke all on function public.set_task_completed_at() from public, anon, authenticated;
create trigger tasks_set_completed_at before insert or update on public.tasks
for each row execute function public.set_task_completed_at();
