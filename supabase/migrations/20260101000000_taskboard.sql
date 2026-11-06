create table public.boards (
  id uuid primary key default gen_random_uuid(),
  name text not null,
  created_by uuid not null default auth.uid() references auth.users(id) on delete cascade,
  task_counter integer not null default 0 check (task_counter >= 0),
  created_at timestamptz not null default now()
);

create table public.board_members (
  board_id uuid not null references public.boards(id) on delete cascade,
  user_id uuid not null references auth.users(id) on delete cascade,
  role text not null default 'member' check (role in ('owner', 'member')),
  created_at timestamptz not null default now(),
  primary key (board_id, user_id)
);
create index board_members_user_id_idx on public.board_members (user_id, board_id);

create table public.tasks (
  id uuid primary key default gen_random_uuid(),
  board_id uuid not null references public.boards(id) on delete cascade,
  title text not null,
  status text not null default 'todo',
  priority text not null default 'low',
  completed boolean not null default false,
  created_at timestamptz not null default now(),
  completed_at timestamptz
);
create index tasks_board_id_idx on public.tasks (board_id);

alter table public.boards enable row level security;
alter table public.board_members enable row level security;
alter table public.tasks enable row level security;
revoke all on public.boards, public.board_members, public.tasks from anon, authenticated;
grant select, insert, delete on public.boards to authenticated;
grant update (name) on public.boards to authenticated;
grant select on public.board_members to authenticated;
grant select, insert, update, delete on public.tasks to authenticated;

-- The owner can inspect memberships without recursive board_members policies.
create function public.is_board_member(p_board_id uuid)
returns boolean language sql stable security definer set search_path = ''
as $$
  select exists (select 1 from public.board_members where board_id = p_board_id and user_id = (select auth.uid()));
$$;
revoke all on function public.is_board_member(uuid) from public, anon;
grant execute on function public.is_board_member(uuid) to authenticated;

create function public.add_board_owner()
returns trigger language plpgsql security definer set search_path = ''
as $$
begin
  insert into public.board_members (board_id, user_id, role) values (new.id, new.created_by, 'owner');
  return new;
end;
$$;
revoke all on function public.add_board_owner() from public, anon, authenticated;
create trigger board_add_owner after insert on public.boards for each row execute function public.add_board_owner();

create policy boards_select on public.boards for select to authenticated using (public.is_board_member(id));
create policy boards_insert on public.boards for insert to authenticated with check (created_by = (select auth.uid()));
create policy boards_update on public.boards for update to authenticated using (public.is_board_member(id)) with check (public.is_board_member(id));
create policy boards_delete on public.boards for delete to authenticated using (created_by = (select auth.uid()) and public.is_board_member(id));
create policy members_select on public.board_members for select to authenticated using (public.is_board_member(board_id));
create policy tasks_select on public.tasks for select to authenticated using (public.is_board_member(board_id));
create policy tasks_insert on public.tasks for insert to authenticated with check (public.is_board_member(board_id));
create policy tasks_update on public.tasks for update to authenticated using (public.is_board_member(board_id)) with check (public.is_board_member(board_id));
create policy tasks_delete on public.tasks for delete to authenticated using (public.is_board_member(board_id));

create function public.increment_board_counter(p_board_id uuid)
returns integer language plpgsql security definer set search_path = ''
as $$
declare result integer;
begin
  if not public.is_board_member(p_board_id) then
    raise exception 'board membership required' using errcode = '42501';
  end if;
  update public.boards set task_counter = task_counter + 1 where id = p_board_id returning task_counter into result;
  return result;
end;
$$;
revoke all on function public.increment_board_counter(uuid) from public, anon;
grant execute on function public.increment_board_counter(uuid) to authenticated;
