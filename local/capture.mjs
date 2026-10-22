// 2021 원본(JSP) 화면 캡처 - headless, 파일로만 저장 (창 안 띄움)
// node --experimental-websocket capture.mjs [base] [outDir]
import { spawn } from 'node:child_process';
import { mkdirSync, writeFileSync } from 'node:fs';
const BASE = process.argv[2] || 'http://localhost:8099';
const OUT = process.argv[3] || new URL('../docs/screenshots/', import.meta.url).pathname;
mkdirSync(OUT, { recursive: true });
const chrome = spawn('/Applications/Google Chrome.app/Contents/MacOS/Google Chrome', ['--headless=new', '--hide-scrollbars', '--remote-debugging-port=9350', `--user-data-dir=/tmp/eureka-cap-${Date.now()}`, 'about:blank'], { stdio: 'ignore' });
const sleep = ms => new Promise(r => setTimeout(r, ms));
let ws; for (let i = 0; i < 40 && !ws; i++) { try { ws = (await (await fetch('http://127.0.0.1:9350/json')).json()).find(x => x.type === 'page')?.webSocketDebuggerUrl } catch {} await sleep(250); }
const s = new WebSocket(ws); await new Promise(r => s.onopen = r); let id = 0; const P = new Map(); const errs = [];
s.onmessage = e => { const m = JSON.parse(e.data); if (m.id && P.has(m.id)) { P.get(m.id)(m.result); P.delete(m.id); }
  if (m.method === 'Runtime.exceptionThrown') errs.push(m.params.exceptionDetails.exception?.description?.split('\n')[0]); };
const send = (m, p = {}) => new Promise((r, j) => { const i = ++id; P.set(i, r); s.send(JSON.stringify({ id: i, method: m, params: p })); setTimeout(() => P.has(i) && (P.delete(i), j(new Error(m))), 30000); });
const ev = async e => (await send('Runtime.evaluate', { expression: e, returnByValue: true, awaitPromise: true })).result.value;
await send('Runtime.enable'); await send('Page.enable');
await send('Emulation.setDeviceMetricsOverride', { width: 1440, height: 900, deviceScaleFactor: 2, mobile: false });
const go = async (p, wait = 3500) => { await send('Page.navigate', { url: BASE + p }); await sleep(wait); };
const shot = async (name, full = false) => {
  const r = await send('Page.captureScreenshot', { format: 'jpeg', quality: 88, captureBeyondViewport: full });
  writeFileSync(OUT + name + '.jpg', Buffer.from(r.data, 'base64')); console.log('저장', name);
};
const scrollTo = async y => { await ev(`window.scrollTo(0, ${y})`); await sleep(1200); };

// 1) 메인 - 첫 화면(배너), 내려가며 섹션별, 전체
await go('/form/mainPage.jsp', 5000);
await shot('01-main-banner');
await ev(`$('.mainMenu li[rel=area_tab]').trigger('mouseover'); $('.subMenu').show(); $('#area_tab').show(); $('#area_tab .list>li:first .detail_thema').show()`); await sleep(800);
await shot('02-main-megamenu');
await ev(`$('.subMenu').hide()`);
for (const [name, sel] of [['03-main-merit', '.pageMerit'], ['04-main-bestpick', '.banner h1'], ['05-main-recommend', '.recommendTravel'], ['06-main-ad', '.bannerAd']]) {
  const y = await ev(`(document.querySelector('${sel}')?.getBoundingClientRect().top || 0) + scrollY - 120`); await scrollTo(y); await shot(name);
}
await scrollTo(0); await shot('07-main-full', true);

// 2) 목록 - 여행가자 > 나홀로 여행 > 바다로 (detail_option_no=2), 그다음 읽기 창
await go('/form/boardList.jsp?detail_option_no=2'); await shot('08-boardlist');
await go('/form/boardList.jsp?detail_option_no=35'); await shot('09-boardlist-food');
await ev(`(document.querySelector('#boardList a, #boardList td img'))?.click()`); await sleep(2500);
await shot('10-read-popup');

// 3) 로그인·회원가입 탭
await go('/form/loginAndJoin.jsp'); await shot('11-login');
await ev(`$('.tabMenu li[rel=join]').trigger('click')`); await sleep(800); await shot('12-join');

// 4) 로그인 후 마이페이지 · 글쓰기 창
await go('/form/loginAndJoin.jsp');
await ev(`document.querySelector('#userId').value='eureka'; document.querySelector('#userPw').value='eureka1234'; document.querySelector('#login form, form[action*=loginOk]').submit()`); await sleep(3500);
await go('/form/myPage.jsp'); await shot('13-mypage');
await go('/form/mainPage.jsp', 4000);
await ev(`$('.writeBoard').first().trigger('click')`); await sleep(1500); await shot('14-write-modal');

console.log('스크립트 예외', errs.length ? [...new Set(errs)].slice(0, 5) : '없음');
s.close(); chrome.kill(); process.exit(0);
