# AnimeTV

![AnimeTV repository banner](docs/branding/animetv-banner.svg)

<p align="center">
  <strong>A modern anime hub for Web and Android TV with multi-source playback, Japanese audio preference, and Spanish subtitle support.</strong>
</p>

<p align="center">
  <a href="https://animetv-umber.vercel.app">Live Demo</a>
  ·
  <a href="docs/API.md">API Docs</a>
  ·
  <a href="docs/ANDROID_TV.md">Android TV</a>
  ·
  <a href="docs/TROUBLESHOOTING.md">Troubleshooting</a>
</p>

<p align="center">
  <img alt="Node.js" src="https://img.shields.io/badge/Node.js-18%2B-40dfc2?style=for-the-badge&logo=node.js&logoColor=white">
  <img alt="Android TV" src="https://img.shields.io/badge/Android%20TV-ready-8a5cff?style=for-the-badge&logo=android&logoColor=white">
  <img alt="License" src="https://img.shields.io/badge/License-MIT-f2bb46?style=for-the-badge">
  <img alt="Playback" src="https://img.shields.io/badge/Playback-multi--source-00d2ff?style=for-the-badge">
</p>

## Highlights

- TV-first interface with compact sidebar navigation and remote-friendly focus states.
- Multi-source catalogs: AniList/Jikan metadata, AniPub, AllAnime, JIMOV/TioAnime, and custom addons.
- Cinema mode player with opaque episode header, prev/next navigation, and seek controls.
- Embedded iframe playback and direct `<video>` playback stay separated for safety.
- Japanese audio and Spanish subtitles are the default playback preference.
- English subtitle tracks can be translated into Spanish for direct subtitle files.
- Favorites, watch history, resume positions, settings, light/dark theme, daily refresh, and Android TV wrapper.
- Windows launcher can supervise AnimeTV and local companion services, restarting after crashes.

## Live Deployment

Production is deployed on Vercel:

```text
https://animetv-umber.vercel.app
```

The app is also runnable locally at:

```text
http://127.0.0.1:4173
```

## Quick Start (local)

```powershell
npm start
```

For the full local setup with server monitoring:

```powershell
.\start-all.bat
```

Useful options:

```powershell
.\start-all.bat -NoBrowser
.\start-all.bat -Anime1vPath "C:\anime1v-api"
```

## Deploy to Vercel

### 1. Import the repo

Go to [vercel.com/new](https://vercel.com/new), import this GitHub repository.

Vercel will auto-detect the settings from `vercel.json`:

| Setting | Value |
|---|---|
| Build Command | `npm run vercel-build` |
| Output Directory | `dist` |
| Install Command | *(leave blank — no npm dependencies)* |

### 2. Environment variables (Vercel dashboard → Settings → Environment Variables)

Only these are required for the default online experience:

| Variable | Required | Default | Notes |
|---|---|---|---|
| `JIMOV_API` | No | `https://jimov-api.vercel.app` | Override if you self-host JIMOV |

Everything else is optional. AllAnime and AniPub work with no keys.

Optional integrations:

| Variable | Notes |
|---|---|
| `CONSUMET_API` | Public HTTPS Consumet instance (e.g. deployed on Railway). Skip for Vercel — `localhost:3000` won't work. |
| `RAPIDAPI_ANIME_HOST` | Your RapidAPI host for the anime streaming API. |
| `RAPIDAPI_ANIME_KEY` | Your RapidAPI key. Both must be set together. |
| `ANIME1V_API` | Public HTTPS Anime1v-compatible API. Skip for Vercel — `localhost:3001` is local only. |
| `LOG_LEVEL` | `debug` / `info` / `warn` / `error` / `silent`. Default: `info`. |
| `UPDATE_REPO_URL` | GitHub repo URL for the in-app update checker. |

### 3. Deploy

```bash
git push origin main   # Vercel auto-deploys on push
```

Or manually from the Vercel dashboard → **Redeploy**.

### 4. Verify

Open `https://your-deployment.vercel.app/api/health` — you should see `{"ok":true,"app":"AnimeTV","api":"ready",...}`.

## Local development setup

```powershell
# 1. Clone
git clone https://github.com/JSolanoDev/AnimeTV.git
cd AnimeTV

# 2. Copy env file
cp .env.example .env.local

# 3. Start the server
npm start
# → http://localhost:4173
```

No `npm install` is needed — this project has zero runtime npm dependencies.

## Build Android APK

```powershell
cd android
.\gradlew.bat assembleDebug
```

APK output:

```text
android\app\build\outputs\apk\debug\app-debug.apk
```

## Sources

| Source | Type | Playback | Needs key? |
|---|---|---|---|
| AniList + Jikan | Metadata only | — | No |
| AniPub | Online addon | Iframe | No |
| AllAnime | Online API | Direct / iframe | No |
| JIMOV / TioAnime | Online addon | Direct / iframe | No |
| Consumet KickAssAnime | Self-hosted | HLS / M3U8 | No (self-host) |
| RapidAPI Anime | API addon | HLS / M3U8 | Yes (paid) |
| Custom sources | User configured | Direct / iframe | — |

## Playback Safety

AnimeTV only sends direct `.mp4`, `.m3u8`, `videoUrl`, `streamUrl`, or `file` values to the main `<video>` player.

Iframe embeds are handled separately:

- Server returns `externalUrl` + `externalType: "iframe"`.
- Client detects iframe episodes before direct playback.
- Iframe embeds render inside the AnimeTV embedded iframe container.
- Direct video and iframe playback paths stay separate.

## API

Core endpoints:

```text
GET /api/health
GET /api/catalog
GET /api/anipub/catalog/all?limit=12000
GET /api/anipub/episodes/:id
GET /api/allanime/search?q=naruto&limit=8
GET /api/allanime/watch?id=SHOW_ID&ep=1&lang=sub
GET /api/jimov/tioanime/catalog
GET /api/jimov/tioanime/health
GET /api/rapid-anime/health
GET /api/rapid-anime/catalog
GET /api/refresh-daily?background=1
```

More details are in [docs/API.md](docs/API.md).

## Development commands

```powershell
npm run check          # syntax check + security audit
npm run vercel-build   # build dist/ and public/ from root sources
npm run android:build  # build Android APK
```

## Project Structure

```text
AnimeTV/
├── animetv-local.js      # Local server entrypoint (npm start)
├── animetv-server.js     # Shared API/server handler (used by local + Vercel)
├── client.js             # Frontend application
├── styles.css            # TV-first UI styles
├── index.html            # App shell
├── sources.json          # Default source configuration
├── api/[...path].js      # Vercel serverless API bridge
├── scripts/
│   ├── build-static.mjs  # Copies static assets → dist/ and public/
│   └── security-audit.mjs
├── js/                   # Frontend constants, utils, normalizer, translations
├── android/              # Android TV wrapper
└── docs/                 # API docs, branding, troubleshooting
```

## Maintainer

Built by [JSolanoDev](https://github.com/JSolanoDev).

## License

MIT. See [LICENSE](LICENSE).
