// 2021 원본(JSP) 움직이는 장면 녹화 - headless 화면 프레임(screencast)을 모아 GIF 로 (창 안 띄움)
// 장면 1: 메인 페이지 위 → 아래 스크롤 / 장면 2: 로그인 → 글쓰기 창 → 이미지 → 업로드 → 목록에 새 글
import { spawn, execFileSync } from 'node:child_process';
import { mkdirSync, writeFileSync, rmSync, readFileSync } from 'node:fs';
const BASE = 'http://localhost:8099';
const DIR = new URL('./', import.meta.url).pathname;
const OUT = DIR + '../docs/screenshots/';
const SRC_IMG = DIR + '../webProject/src/main/webapp/images/recommendImages/';
// 레포 안 경로의 파일을 그대로 넣으면 headless Chrome 이 업로드 요청에서 ERR_ACCESS_DENIED → 원본 alert("fail") 로 글이 저장되지 않음
// (정확한 원인은 미확인). /tmp 로 복사한 파일은 정상 업로드됨
const IMG = '/tmp/eureka-upload/';
mkdirSync(IMG, { recursive: true });
for (const f of ['gangneung2.jpg', 'gangneung.jpg']) writeFileSync(IMG + f, readFileSync(SRC_IMG + f));
const chrome = spawn('/Applications/Google Chrome.app/Contents/MacOS/Google Chrome', ['--headless=new', '--hide-scrollbars', '--remote-debugging-port=9351', `--user-data-dir=/tmp/eureka-rec-${Date.now()}`, 'about:blank'], { stdio: 'ignore' });
const sleep = ms => new Promise(r => setTimeout(r, ms));
let ws; for (let i = 0; i < 40 && !ws; i++) { try { ws = (await (await fetch('http://127.0.0.1:9351/json')).json()).find(x => x.type === 'page')?.webSocketDebuggerUrl } catch {} await sleep(250); }
const s = new WebSocket(ws); await new Promise(r => s.onopen = r); let id = 0; const P = new Map();
let frames = null; let frameDir = '';
s.onmessage = e => { const m = JSON.parse(e.data); if (m.id && P.has(m.id)) { P.get(m.id)(m.result); P.delete(m.id); }
  if (m.method === 'Page.screencastFrame') {
    if (frames) { frames.push(Date.now()); writeFileSync(`${frameDir}/${String(frames.length).padStart(5, '0')}.jpg`, Buffer.from(m.params.data, 'base64')); }
    send('Page.screencastFrameAck', { sessionId: m.params.sessionId }); } };
const send = (m, p = {}) => new Promise((r, j) => { const i = ++id; P.set(i, r); s.send(JSON.stringify({ id: i, method: m, params: p })); setTimeout(() => P.has(i) && (P.delete(i), j(new Error(m))), 30000); });
const ev = async e => (await send('Runtime.evaluate', { expression: e, returnByValue: true, awaitPromise: true })).result.value;
await send('Page.enable'); await send('DOM.enable'); await send('Runtime.enable');
await send('Emulation.setDeviceMetricsOverride', { width: 1440, height: 900, deviceScaleFactor: 1, mobile: false });
const go = async (p, wait = 3500) => { await send('Page.navigate', { url: BASE + p }); await sleep(wait); };
const startRec = async name => { frameDir = `/tmp/eureka-frames-${name}`; rmSync(frameDir, { recursive: true, force: true }); mkdirSync(frameDir); frames = [];
  await send('Page.startScreencast', { format: 'jpeg', quality: 80, maxWidth: 1440, maxHeight: 900, everyNthFrame: 1 }); };
const stopRec = async name => { await send('Page.stopScreencast'); const ts = frames; frames = null;
  writeFileSync(`${frameDir}/times.json`, JSON.stringify(ts));
  execFileSync('python3', [DIR + 'frames2gif.py', frameDir, OUT + name + '.gif', ...(GIF_OPTS[name] || [])], { stdio: 'inherit' }); };
const GIF_OPTS = { 'main-scroll': ['720', '10'], 'write-flow': ['800', '12'] };
// 사람처럼 천천히 스크롤
const smoothScroll = async (to, ms) => ev(`new Promise(r=>{const s=scrollY,t0=performance.now();const f=t=>{const k=Math.min(1,(t-t0)/${ms});scrollTo(0,s+(${to}-s)*(k<.5?2*k*k:1-Math.pow(-2*k+2,2)/2));k<1?requestAnimationFrame(f):r()};requestAnimationFrame(f)})`);

// 장면 1: 메인 스크롤
await go('/form/mainPage.jsp', 5000);
await startRec('main-scroll'); await sleep(1500);
const H = await ev(`document.documentElement.scrollHeight - innerHeight`);
await smoothScroll(H, 9000); await sleep(1500); await smoothScroll(0, 2500); await sleep(800);
await stopRec('main-scroll');

// 장면 2: 로그인 → 글쓰기 → 업로드 → 목록
await go('/form/loginAndJoin.jsp');
await ev(`document.querySelector('#userId').value='eureka'; document.querySelector('#userPw').value='eureka1234'; document.querySelector('form[action*=loginOk]').submit()`); await sleep(3000);
await go('/form/mainPage.jsp', 4000);
await startRec('write-flow'); await sleep(1000);
await ev(`$('.writeBoard').first().trigger('click')`); await sleep(1500);
await ev(`$('.category').val('여행가자').trigger('change'); $('.combo1').trigger('change')`); await sleep(900);
await ev(`$('.locMain').val('강원도').trigger('change')`); await sleep(1200);
await ev(`$('.locDetail').val('강릉').trigger('change')`); await sleep(1200);
await ev(`document.querySelector("input[name=main_option][value='나홀로 여행']").click()`); await sleep(1100);
await ev(`document.querySelector("input[name=detail_option][value='바다로']").click()`); await sleep(1500);
await ev(`$('.form1 .nextButton').trigger('click')`); await sleep(1500);
const { root } = await send('DOM.getDocument'); const { nodeId } = await send('DOM.querySelector', { nodeId: root.nodeId, selector: '#imagesInput' });
await send('DOM.setFileInputFiles', { nodeId, files: [IMG + 'gangneung2.jpg', IMG + 'gangneung.jpg'] });
await ev(`$('#imagesInput').trigger('change')`); await sleep(3000);
await ev(`(()=>{const t=document.querySelector('#title'); t.value='강릉 경포호 자전거 한 바퀴'; const c=document.querySelector('#txtArea1'); c.value='호수 둘레가 평평해서 자전거 타기 딱 좋아요.\\n해 질 녘에 한 바퀴 추천!'; })()`); await sleep(1800);
// 업로드가 끝나면 mainPage.jsp 로 다시 이동(같은 주소) → 페이지에 표시를 남겨두고 새로 로드돼 사라질 때까지 기다림
await ev('window.__beforeUpload = true'); await ev(`$('.submitButton').trigger('click')`);
for (let i = 0; i < 60 && (await ev('window.__beforeUpload === true')); i++) await sleep(500);
await sleep(2500);
// 등록 뒤 목록(강릉·바다로)에서 새 글 확인 → 읽기 창
await go('/form/boardList.jsp?city_no=4&detail_option_no=2', 3500); await sleep(1500);
await ev(`[...document.querySelectorAll('#boardList td')].find(td=>td.innerText.includes('경포호'))?.querySelector('img,a')?.click()`); await sleep(4000);
await stopRec('write-flow');
console.log('지금 주소:', await ev('location.pathname + location.search'));
s.close(); chrome.kill(); process.exit(0);
