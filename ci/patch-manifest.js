// 把启动入口从 MainActivity 换成 GateActivity（构建时打补丁，不改仓库里的原 manifest）。
// 用法：node ci/patch-manifest.js
const fs = require('fs');
const P = 'app/src/main/AndroidManifest.xml';
let s = fs.readFileSync(P, 'utf8');

const filt = [
  '            <intent-filter>',
  '                <action android:name="android.intent.action.MAIN" />',
  '                <category android:name="android.intent.category.LAUNCHER" />',
  '            </intent-filter>',
  ''
].join('\n');

if (!s.includes(filt)) {
  console.error('ERR: MainActivity 的 launcher intent-filter 没找到，上游可能改过 manifest');
  process.exit(1);
}
s = s.replace(filt, '');

const gate = [
  '        <activity',
  '            android:name=".GateActivity"',
  '            android:exported="true"',
  '            android:theme="@style/Theme.Dsh"',
  '            android:noHistory="true">',
  '            <intent-filter>',
  '                <action android:name="android.intent.action.MAIN" />',
  '                <category android:name="android.intent.category.LAUNCHER" />',
  '            </intent-filter>',
  '        </activity>',
  '',
  ''
].join('\n');

const anchor = '        <activity\n            android:name=".MainActivity"';
if (!s.includes(anchor)) {
  console.error('ERR: MainActivity 的 activity 节点没找到');
  process.exit(1);
}
s = s.replace(anchor, gate + anchor);

fs.writeFileSync(P, s);
console.log('OK: launcher -> GateActivity');
