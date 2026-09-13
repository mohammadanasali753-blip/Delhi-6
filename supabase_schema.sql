create extension if not exists pgcrypto;

create table if not exists menu_items (
 id uuid primary key default gen_random_uuid(),
 name text not null,
 description text,
 price numeric not null,
 image_url text,
 available boolean not null default true,
 created_at timestamptz default now()
);

create table if not exists site_settings (
 id integer primary key,
 map_url text
);
insert into site_settings(id) values(1) on conflict(id) do nothing;

create table if not exists orders (
 id uuid primary key default gen_random_uuid(),
 customer_name text not null,
 phone text not null,
 address text not null,
 items jsonb not null,
 total numeric not null,
 created_at timestamptz default now()
);

alter table menu_items enable row level security;
alter table site_settings enable row level security;
alter table orders enable row level security;

create policy "public can read menu" on menu_items for select using (available = true);
create policy "public can read settings" on site_settings for select using (true);

-- Admin writes require an authenticated Supabase user.
create policy "admin insert menu" on menu_items for insert to authenticated with check (true);
create policy "admin update menu" on menu_items for update to authenticated using (true) with check (true);
create policy "admin delete menu" on menu_items for delete to authenticated using (true);
create policy "admin update settings" on site_settings for update to authenticated using (true) with check (true);
create policy "admin insert orders" on orders for insert to anon,authenticated with check (true);
create policy "admin read orders" on orders for select to authenticated using (true);
