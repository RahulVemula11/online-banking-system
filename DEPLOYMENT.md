# Deployment Guide — Getting the Live Demo Online (Free)

This walks you through putting the app live so your portfolio can link to it.
We'll use **Render** (backend + free Postgres) and **Vercel** (frontend). Both
have free tiers and connect straight to GitHub.

> Heads-up on the free tier: Render's free web service **sleeps after ~15 minutes
> of inactivity** and takes ~30–50 seconds to wake on the next visit. That's fine
> for a portfolio demo — just know the first load may be slow. Mention this isn't
> a limitation of the app, only the free hosting.

---

## Step 0 — Put the code on GitHub

You do this part (I can't access your GitHub). On your machine, inside the
`banking-system` folder:

```bash
git init
git add .
git commit -m "Online banking system: Spring Boot + React"
```

Then create an **empty** repo on github.com (no README), and follow the two
lines GitHub shows you, which look like:

```bash
git remote add origin https://github.com/RahulVemula11/online-banking-system.git
git branch -M main
git push -u origin main
```

---

## Step 1 — Deploy the backend + database on Render

1. Go to **render.com** and sign in with GitHub.
2. Click **New → Blueprint**.
3. Select your `online-banking-system` repo. Render reads `render.yaml` and
   proposes a **web service** (the API) plus a **Postgres database** — both free.
4. Click **Apply**. Render builds the Docker image and provisions the database.
   First build takes a few minutes.
5. When it's live, copy the API URL — something like
   `https://aegis-bank-api.onrender.com`.

The `JWT_SECRET` is auto-generated and the database credentials are wired in
automatically by the blueprint, so there's nothing to paste.

**Test it:** open `https://YOUR-API.onrender.com/api/auth/login` — you should get
a JSON error (not a crash), which means the API is up.

---

## Step 2 — Deploy the frontend on Vercel

1. Go to **vercel.com** and sign in with GitHub.
2. Click **Add New → Project** and import the same repo.
3. Set **Root Directory** to `frontend`.
4. Vercel auto-detects Vite. Before deploying, add an **Environment Variable**:
   - Name: `VITE_API_URL`
   - Value: your Render API URL from Step 1 (e.g. `https://aegis-bank-api.onrender.com`)
5. Click **Deploy**. You'll get a URL like `https://aegis-bank.vercel.app`.

---

## Step 3 — Connect the two (CORS)

Back in Render, open your API service → **Environment** → set:

- `FRONTEND_ORIGIN` = your Vercel URL (e.g. `https://aegis-bank.vercel.app`)

Save. Render redeploys. Now the backend allows your frontend to call it.

---

## Step 4 — Verify

Open your Vercel URL, log in as `alice` / `password123`, deposit some money, and
transfer to `BANK10000002`. If the history updates, you're fully live.

---

## Step 5 — Update your portfolio

In your portfolio `index.html`, the banking project's link currently points at
your GitHub profile. Update both:

- **"View on GitHub"** → your repo: `https://github.com/RahulVemula11/online-banking-system`
- Add a **"Live Demo"** link → your Vercel URL

(I can make these HTML edits for you — just give me the final URLs once deployed.)

---

## Troubleshooting

- **Frontend loads but login fails / CORS error** → `FRONTEND_ORIGIN` on Render
  doesn't exactly match your Vercel URL (check https vs trailing slash).
- **First request very slow** → free Render service waking from sleep. Normal.
- **Build fails on Render** → check the build logs; usually a Java version
  mismatch. The Dockerfile pins Java 17, so this should be rare.
- **"relation does not exist"** → the first run creates tables automatically
  (`ddl-auto=update`); give it a moment after the DB provisions.
