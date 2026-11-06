-- DEFECT-RLS-001: permissive policies are ORed, exposing tasks from every board.
create policy defect_cross_board_task_read on public.tasks for select to authenticated using (true);

-- DEFECT-RACE-001: separate read and write loses increments under concurrency.
create or replace function public.increment_board_counter(p_board_id uuid)
returns integer language plpgsql security definer set search_path = ''
as $$
declare result integer;
begin
  if not public.is_board_member(p_board_id) then
    raise exception 'board membership required' using errcode = '42501';
  end if;
  select task_counter into result from public.boards where id = p_board_id;
  result := result + 1;
  update public.boards set task_counter = result where id = p_board_id;
  return result;
end;
$$;
