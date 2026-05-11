// Plasma Files — a mobile file manager that borrows the working metaphors
// of a desktop file explorer (sidebar of pinned locations, breadcrumb path,
// multi-column details list, ribbon-style action bar, properties pane,
// open-tabs strip) and translates them to a phone.

const { useState, useMemo, useEffect, useRef } = React;

// ──────────────────────────────────────────────────────────
// Design tokens (original, warm-neutral + teal accent)
// ──────────────────────────────────────────────────────────
const PALETTES = {
  light: {
    bg:        '#f3efe9',        // warm paper
    chrome:    'rgba(248,245,240,0.86)',
    panel:     '#fbf9f5',
    sunken:    '#ebe6dd',
    line:      '#dcd5c7',
    softLine:  '#e7e1d4',
    text:      '#221f1a',
    sub:       '#6b6356',
    mute:      '#9a9181',
    accent:    '#127a72',        // deep teal
    accentSoft:'#d5ece9',
    selected:  '#dff1ee',
    hover:     '#efe9dc',
    warn:      '#a85b29',
  },
  dark: {
    bg:        '#1c1b18',
    chrome:    'rgba(28,27,24,0.86)',
    panel:     '#26241f',
    sunken:    '#181714',
    line:      '#39362f',
    softLine:  '#2e2c26',
    text:      '#ece7dc',
    sub:       '#a39c8d',
    mute:      '#6a6457',
    accent:    '#5ec8bc',
    accentSoft:'#173834',
    selected:  '#1d3a36',
    hover:     '#2f2c25',
    warn:      '#e29565',
  },
};

const ACCENTS = {
  teal:    { light: '#127a72', dark: '#5ec8bc', soft_l: '#d5ece9', soft_d: '#173834', sel_l: '#dff1ee', sel_d: '#1d3a36' },
  amber:   { light: '#a36a14', dark: '#e3b46b', soft_l: '#f1e3c8', soft_d: '#382a14', sel_l: '#f6ead0', sel_d: '#3a2c16' },
  violet:  { light: '#6a4ac8', dark: '#a78cf2', soft_l: '#e2dbf6', soft_d: '#231a3f', sel_l: '#e9e2f8', sel_d: '#251c44' },
  rose:    { light: '#b1456e', dark: '#e98fae', soft_l: '#f3d8e1', soft_d: '#3a1c27', sel_l: '#f6dde5', sel_d: '#3f1f2b' },
};

function makeTheme(mode, accentName) {
  const base = PALETTES[mode];
  const a = ACCENTS[accentName];
  return {
    ...base,
    accent:     mode === 'light' ? a.light  : a.dark,
    accentSoft: mode === 'light' ? a.soft_l : a.soft_d,
    selected:   mode === 'light' ? a.sel_l  : a.sel_d,
  };
}

// ──────────────────────────────────────────────────────────
// Mock filesystem
// ──────────────────────────────────────────────────────────
const FS = {
  '/': {
    type: 'root',
    children: [
      { name: 'Internal storage', kind: 'volume', path: '/storage', total: 128, used: 71.2 },
      { name: 'SD card',          kind: 'volume', path: '/sdcard',  total: 64,  used: 12.8 },
    ],
  },
  '/storage': {
    type: 'folder',
    children: [
      { name: 'Documents',     kind: 'folder', items: 38,  modified: 'May 9, 2026 · 14:02' },
      { name: 'Downloads',     kind: 'folder', items: 142, modified: 'May 10, 2026 · 09:48' },
      { name: 'Pictures',      kind: 'folder', items: 1284,modified: 'May 8, 2026 · 21:11' },
      { name: 'Music',         kind: 'folder', items: 612, modified: 'Apr 30, 2026 · 10:22' },
      { name: 'Movies',        kind: 'folder', items: 27,  modified: 'Apr 21, 2026 · 18:30' },
      { name: 'Recordings',    kind: 'folder', items: 9,   modified: 'May 7, 2026 · 07:14' },
      { name: 'Android',       kind: 'folder', items: 4,   modified: 'Mar 02, 2026 · 12:00', system: true },
    ],
  },
  '/storage/Documents': {
    type: 'folder',
    children: [
      { name: 'Q2 board memo.docx',          kind: 'doc',   ext: 'docx', size: '184 KB',  modified: 'May 9, 2026 · 14:02' },
      { name: 'Lease — 2026 renewal.pdf',    kind: 'pdf',   ext: 'pdf',  size: '2.1 MB',  modified: 'May 4, 2026 · 11:47' },
      { name: 'Receipts',                     kind: 'folder', items: 23,                  modified: 'May 2, 2026 · 16:08' },
      { name: 'Tax return 2025.pdf',         kind: 'pdf',   ext: 'pdf',  size: '1.4 MB',  modified: 'Apr 18, 2026 · 09:30' },
      { name: 'Recipes.txt',                 kind: 'text',  ext: 'txt',  size: '6 KB',    modified: 'Apr 12, 2026 · 20:55' },
      { name: 'Pitch deck v3.pptx',          kind: 'slide', ext: 'pptx', size: '9.8 MB',  modified: 'Apr 11, 2026 · 13:21' },
      { name: 'Budget.xlsx',                 kind: 'sheet', ext: 'xlsx', size: '74 KB',   modified: 'Apr 09, 2026 · 17:02' },
      { name: 'screenshot_05_09.png',        kind: 'image', ext: 'png',  size: '512 KB',  modified: 'May 9, 2026 · 14:30' },
      { name: 'logo.svg',                    kind: 'image', ext: 'svg',  size: '8 KB',    modified: 'Mar 22, 2026 · 09:00' },
      { name: 'site-backup.zip',             kind: 'archive', ext: 'zip', size: '46 MB',  modified: 'Feb 14, 2026 · 22:18' },
    ],
  },
  '/storage/Downloads': {
    type: 'folder',
    children: [
      { name: 'invoice-4821.pdf',            kind: 'pdf',   ext: 'pdf',  size: '210 KB',  modified: 'May 10, 2026 · 09:48' },
      { name: 'rider-update.apk',            kind: 'app',   ext: 'apk',  size: '38 MB',   modified: 'May 10, 2026 · 09:12' },
      { name: 'episode-214.mp3',             kind: 'audio', ext: 'mp3',  size: '54 MB',   modified: 'May 9, 2026 · 22:01' },
      { name: 'gardening-notes.md',          kind: 'text',  ext: 'md',   size: '12 KB',   modified: 'May 9, 2026 · 19:33' },
      { name: 'invoice-template.docx',       kind: 'doc',   ext: 'docx', size: '92 KB',   modified: 'May 8, 2026 · 11:05' },
      { name: 'mountain-trip.mp4',           kind: 'video', ext: 'mp4',  size: '418 MB',  modified: 'May 7, 2026 · 16:24' },
      { name: 'screenshot_05_07.png',        kind: 'image', ext: 'png',  size: '1.1 MB',  modified: 'May 7, 2026 · 10:12' },
      { name: 'fonts.zip',                   kind: 'archive', ext: 'zip', size: '22 MB',  modified: 'May 4, 2026 · 14:39' },
    ],
  },
  '/storage/Pictures': {
    type: 'folder',
    children: [
      { name: 'Camera',     kind: 'folder', items: 942, modified: 'May 10, 2026 · 08:22' },
      { name: 'Screenshots',kind: 'folder', items: 218, modified: 'May 9, 2026 · 14:30' },
      { name: 'Saved',      kind: 'folder', items: 124, modified: 'May 5, 2026 · 17:10' },
    ],
  },
  '/storage/Documents/Receipts': {
    type: 'folder',
    children: [
      { name: 'starbucks-may-2.jpg', kind: 'image', ext: 'jpg', size: '420 KB', modified: 'May 2, 2026 · 09:15' },
      { name: 'gas-may-3.pdf',       kind: 'pdf',   ext: 'pdf', size: '180 KB', modified: 'May 3, 2026 · 18:40' },
    ],
  },
  '/sdcard': {
    type: 'folder',
    children: [
      { name: 'Backup',  kind: 'folder', items: 6, modified: 'Apr 02, 2026 · 22:00' },
      { name: 'photos-2024.zip', kind: 'archive', ext: 'zip', size: '1.8 GB', modified: 'Jan 18, 2026 · 11:00' },
    ],
  },
};

const SIDEBAR = [
  { group: 'Quick access', items: [
    { name: 'Home',        path: '/',                  kind: 'home' },
    { name: 'Recent',      path: '__recent',           kind: 'recent' },
    { name: 'Starred',     path: '__starred',          kind: 'star' },
    { name: 'Downloads',   path: '/storage/Downloads', kind: 'download' },
  ]},
  { group: 'This phone', items: [
    { name: 'Documents',   path: '/storage/Documents', kind: 'folder' },
    { name: 'Pictures',    path: '/storage/Pictures',  kind: 'folder' },
    { name: 'Music',       path: '/storage/Music',     kind: 'folder' },
    { name: 'Movies',      path: '/storage/Movies',    kind: 'folder' },
  ]},
  { group: 'Devices', items: [
    { name: 'Internal storage', path: '/storage', kind: 'volume', meta: '71.2 / 128 GB' },
    { name: 'SD card',          path: '/sdcard',  kind: 'volume', meta: '12.8 / 64 GB'  },
  ]},
];

const RECENT = [
  { name: 'screenshot_05_09.png', kind: 'image', ext: 'png',  size: '512 KB', modified: 'today · 14:30', parent: '/storage/Documents' },
  { name: 'invoice-4821.pdf',     kind: 'pdf',   ext: 'pdf',  size: '210 KB', modified: 'today · 09:48', parent: '/storage/Downloads' },
  { name: 'gardening-notes.md',   kind: 'text',  ext: 'md',   size: '12 KB',  modified: 'yesterday',     parent: '/storage/Downloads' },
  { name: 'Q2 board memo.docx',   kind: 'doc',   ext: 'docx', size: '184 KB', modified: 'yesterday',     parent: '/storage/Documents' },
];

window.PlasmaData = { FS, SIDEBAR, RECENT, PALETTES, ACCENTS, makeTheme };
