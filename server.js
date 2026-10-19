import http from 'node:http';
import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const PORT = parseInt(process.env.DEFAULT_APP_PORT || '3000', 10);
const HOST = '0.0.0.0';

const VAULT_APK_PATH = path.join(__dirname, 'vault.apk');
const GRADLE_APK_PATH = path.join(__dirname, 'app', 'build', 'outputs', 'apk', 'debug', 'app-debug.apk');

function getApkFilePath() {
  if (fs.existsSync(VAULT_APK_PATH)) return VAULT_APK_PATH;
  if (fs.existsSync(GRADLE_APK_PATH)) return GRADLE_APK_PATH;
  return null;
}

const server = http.createServer((req, res) => {
  const url = new URL(req.url, `http://${req.headers.host || 'localhost'}`);
  const pathname = url.pathname;

  // Handle APK Downloads
  if (pathname === '/app-debug.apk' || pathname === '/vault.apk' || pathname === '/app.apk' || pathname === '/download') {
    const targetApk = getApkFilePath();
    if (!targetApk) {
      res.writeHead(404, { 'Content-Type': 'text/plain' });
      res.end('APK file not found. Please compile the Android project first.');
      return;
    }

    const stat = fs.statSync(targetApk);
    const fileName = pathname === '/vault.apk' ? 'vault.apk' : (pathname === '/app.apk' ? 'app.apk' : 'app-debug.apk');

    res.writeHead(200, {
      'Content-Type': 'application/vnd.android.package-archive',
      'Content-Length': stat.size,
      'Content-Disposition': `attachment; filename="${fileName}"`,
      'Cache-Control': 'no-cache'
    });

    const readStream = fs.createReadStream(targetApk);
    readStream.pipe(res);
    return;
  }

  // Health check endpoint
  if (pathname === '/healthz' || pathname === '/api/health') {
    res.writeHead(200, { 'Content-Type': 'application/json' });
    res.end(JSON.stringify({ status: 'ok', apkAvailable: !!getApkFilePath() }));
    return;
  }

  // Main page (served from index.html)
  try {
    const htmlPath = path.join(__dirname, 'index.html');
    const htmlContent = fs.readFileSync(htmlPath, 'utf8');
    res.writeHead(200, {
      'Content-Type': 'text/html; charset=utf-8',
      'Cache-Control': 'no-cache'
    });
    res.end(htmlContent);
  } catch (err) {
    res.writeHead(500, { 'Content-Type': 'text/plain' });
    res.end('Error loading index.html: ' + err.message);
  }
});

server.listen(PORT, HOST, () => {
  console.log(`[MyVault Server] Running at http://${HOST}:${PORT}`);
  console.log(`[MyVault Server] APK available at: ${getApkFilePath()}`);
});
