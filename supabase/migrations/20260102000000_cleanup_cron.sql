create extension if not exists pg_cron with schema pg_catalog;

select cron.schedule(
  'taskboard-prune-completed',
  '0 3 * * *',
  $job$delete from public.tasks where completed = true and completed_at < now() - interval '30 days'$job$
);
