const CACHE = 'heb-clock-v1';
const ASSETS = [
  '/heb-clock-web/',
  '/heb-clock-web/index.html',
  '/heb-clock-web/fonts/fridge_Regular.ttf',
  '/heb-clock-web/icons/icon-196.png',
  '/heb-clock-web/icons/icon-512.png',
  '/heb-clock-web/icons/icon-2048.png',
];

self.addEventListener('install', e => {
  e.waitUntil(
    caches.open(CACHE).then(c => c.addAll(ASSETS))
  );
  self.skipWaiting();
});

self.addEventListener('activate', e => {
  e.waitUntil(
    caches.keys().then(keys =>
      Promise.all(keys.filter(k => k !== CACHE).map(k => caches.delete(k)))
    )
  );
  self.clients.claim();
});

self.addEventListener('fetch', e => {
  e.respondWith(
    caches.match(e.request).then(cached => cached || fetch(e.request))
  );
});
