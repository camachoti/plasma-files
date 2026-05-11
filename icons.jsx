// Icon set — original geometric icons drawn from primitives only.
// All icons take `size` and `color`. We avoid complex SVG paths.

const Icon = {
  // generic
  chevron: ({size=16, color='currentColor', dir='right'}) => {
    const r = {right:0, down:90, left:180, up:270}[dir];
    return (
      <svg width={size} height={size} viewBox="0 0 16 16" style={{transform:`rotate(${r}deg)`}}>
        <path d="M5.5 3l5 5-5 5" fill="none" stroke={color} strokeWidth="1.6" strokeLinecap="round" strokeLinejoin="round"/>
      </svg>
    );
  },
  back: ({size=20, color='currentColor'}) => (
    <svg width={size} height={size} viewBox="0 0 20 20">
      <path d="M12 4l-6 6 6 6" fill="none" stroke={color} strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round"/>
    </svg>
  ),
  forward: ({size=20, color='currentColor'}) => (
    <svg width={size} height={size} viewBox="0 0 20 20">
      <path d="M8 4l6 6-6 6" fill="none" stroke={color} strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round"/>
    </svg>
  ),
  up: ({size=20, color='currentColor'}) => (
    <svg width={size} height={size} viewBox="0 0 20 20">
      <path d="M4 12l6-6 6 6" fill="none" stroke={color} strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round"/>
    </svg>
  ),
  menu: ({size=20, color='currentColor'}) => (
    <svg width={size} height={size} viewBox="0 0 20 20">
      <rect x="3" y="5"  width="14" height="1.6" rx="0.8" fill={color}/>
      <rect x="3" y="9.2" width="14" height="1.6" rx="0.8" fill={color}/>
      <rect x="3" y="13.4" width="14" height="1.6" rx="0.8" fill={color}/>
    </svg>
  ),
  more: ({size=20, color='currentColor'}) => (
    <svg width={size} height={size} viewBox="0 0 20 20">
      <circle cx="10" cy="4"  r="1.6" fill={color}/>
      <circle cx="10" cy="10" r="1.6" fill={color}/>
      <circle cx="10" cy="16" r="1.6" fill={color}/>
    </svg>
  ),
  search: ({size=20, color='currentColor'}) => (
    <svg width={size} height={size} viewBox="0 0 20 20">
      <circle cx="9" cy="9" r="5.2" fill="none" stroke={color} strokeWidth="1.6"/>
      <path d="M13 13l3.5 3.5" stroke={color} strokeWidth="1.8" strokeLinecap="round"/>
    </svg>
  ),
  close: ({size=18, color='currentColor'}) => (
    <svg width={size} height={size} viewBox="0 0 18 18">
      <path d="M4 4l10 10M14 4L4 14" stroke={color} strokeWidth="1.6" strokeLinecap="round"/>
    </svg>
  ),
  plus: ({size=18, color='currentColor'}) => (
    <svg width={size} height={size} viewBox="0 0 18 18">
      <path d="M9 3v12M3 9h12" stroke={color} strokeWidth="1.7" strokeLinecap="round"/>
    </svg>
  ),
  check: ({size=14, color='#fff'}) => (
    <svg width={size} height={size} viewBox="0 0 14 14">
      <path d="M3 7.2l2.8 2.8L11 4.5" fill="none" stroke={color} strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
    </svg>
  ),

  // ribbon actions — flat geometric
  copy: ({size=18, color='currentColor'}) => (
    <svg width={size} height={size} viewBox="0 0 20 20">
      <rect x="3" y="3" width="11" height="13" rx="1.6" fill="none" stroke={color} strokeWidth="1.5"/>
      <rect x="6.5" y="6.5" width="11" height="13" rx="1.6" fill="#fff" stroke={color} strokeWidth="1.5"/>
    </svg>
  ),
  cut: ({size=18, color='currentColor'}) => (
    <svg width={size} height={size} viewBox="0 0 20 20">
      <circle cx="6" cy="14" r="2.4" fill="none" stroke={color} strokeWidth="1.5"/>
      <circle cx="14" cy="14" r="2.4" fill="none" stroke={color} strokeWidth="1.5"/>
      <path d="M7.8 12.2L15 4M12.2 12.2L5 4" stroke={color} strokeWidth="1.5" strokeLinecap="round"/>
    </svg>
  ),
  paste: ({size=18, color='currentColor'}) => (
    <svg width={size} height={size} viewBox="0 0 20 20">
      <rect x="4" y="4" width="12" height="13" rx="1.6" fill="none" stroke={color} strokeWidth="1.5"/>
      <rect x="7" y="2.5" width="6" height="3" rx="0.8" fill={color}/>
    </svg>
  ),
  trash: ({size=18, color='currentColor'}) => (
    <svg width={size} height={size} viewBox="0 0 20 20">
      <path d="M4 6h12M8 6V4h4v2" stroke={color} strokeWidth="1.5" strokeLinecap="round"/>
      <path d="M5.5 6l1 10h7l1-10" fill="none" stroke={color} strokeWidth="1.5" strokeLinejoin="round"/>
    </svg>
  ),
  share: ({size=18, color='currentColor'}) => (
    <svg width={size} height={size} viewBox="0 0 20 20">
      <circle cx="5" cy="10" r="2.2" fill="none" stroke={color} strokeWidth="1.5"/>
      <circle cx="15" cy="5"  r="2.2" fill="none" stroke={color} strokeWidth="1.5"/>
      <circle cx="15" cy="15" r="2.2" fill="none" stroke={color} strokeWidth="1.5"/>
      <path d="M7 9l6-3M7 11l6 3" stroke={color} strokeWidth="1.5"/>
    </svg>
  ),
  rename: ({size=18, color='currentColor'}) => (
    <svg width={size} height={size} viewBox="0 0 20 20">
      <path d="M3 15l8-8 3 3-8 8H3v-3z" fill="none" stroke={color} strokeWidth="1.5" strokeLinejoin="round"/>
      <path d="M11 5l2-2 3 3-2 2" fill="none" stroke={color} strokeWidth="1.5" strokeLinejoin="round"/>
    </svg>
  ),
  star: ({size=18, color='currentColor', filled=false}) => (
    <svg width={size} height={size} viewBox="0 0 20 20">
      <path d="M10 2.5l2.3 4.8 5.2.6-3.9 3.6 1.1 5.1L10 14l-4.7 2.6 1.1-5.1L2.5 7.9l5.2-.6z"
        fill={filled?color:'none'} stroke={color} strokeWidth="1.4" strokeLinejoin="round"/>
    </svg>
  ),
  sort: ({size=18, color='currentColor'}) => (
    <svg width={size} height={size} viewBox="0 0 20 20">
      <path d="M5 4v12M5 16l-2.2-2.2M5 16l2.2-2.2" stroke={color} strokeWidth="1.5" strokeLinecap="round" fill="none"/>
      <rect x="10" y="4"  width="7" height="1.5" rx="0.7" fill={color}/>
      <rect x="10" y="9"  width="5" height="1.5" rx="0.7" fill={color}/>
      <rect x="10" y="14" width="3" height="1.5" rx="0.7" fill={color}/>
    </svg>
  ),
  view: ({size=18, color='currentColor'}) => (
    <svg width={size} height={size} viewBox="0 0 20 20">
      <rect x="3" y="3" width="6" height="6" rx="1" fill="none" stroke={color} strokeWidth="1.5"/>
      <rect x="11" y="3" width="6" height="6" rx="1" fill="none" stroke={color} strokeWidth="1.5"/>
      <rect x="3" y="11" width="6" height="6" rx="1" fill={color}/>
      <rect x="11" y="11" width="6" height="6" rx="1" fill="none" stroke={color} strokeWidth="1.5"/>
    </svg>
  ),
  filter: ({size=18, color='currentColor'}) => (
    <svg width={size} height={size} viewBox="0 0 20 20">
      <path d="M3 4h14l-5 7v5l-4-1v-4z" fill="none" stroke={color} strokeWidth="1.5" strokeLinejoin="round"/>
    </svg>
  ),
  info: ({size=18, color='currentColor'}) => (
    <svg width={size} height={size} viewBox="0 0 20 20">
      <circle cx="10" cy="10" r="7.5" fill="none" stroke={color} strokeWidth="1.5"/>
      <circle cx="10" cy="6.2" r="1" fill={color}/>
      <rect x="9.1" y="8.6" width="1.8" height="6" rx="0.9" fill={color}/>
    </svg>
  ),
  newfolder: ({size=18, color='currentColor'}) => (
    <svg width={size} height={size} viewBox="0 0 20 20">
      <path d="M2.5 5.5h5l1.5 1.5h8.5v9.5a1 1 0 01-1 1h-13a1 1 0 01-1-1z" fill="none" stroke={color} strokeWidth="1.5" strokeLinejoin="round"/>
      <path d="M13 11.5h3M14.5 10v3" stroke={color} strokeWidth="1.5" strokeLinecap="round"/>
    </svg>
  ),

  // file-type icons — solid tile + extension tag
  folder: ({size=28, color='#e8b962', back='#caa052'}) => (
    <svg width={size} height={size} viewBox="0 0 32 28">
      <path d="M2 6a2 2 0 012-2h7l3 3h14a2 2 0 012 2v15a2 2 0 01-2 2H4a2 2 0 01-2-2z" fill={back}/>
      <path d="M2 10h28v13a2 2 0 01-2 2H4a2 2 0 01-2-2z" fill={color}/>
    </svg>
  ),
  volume: ({size=28, color='currentColor'}) => (
    <svg width={size} height={size} viewBox="0 0 32 28">
      <rect x="3" y="6" width="26" height="16" rx="2" fill="none" stroke={color} strokeWidth="1.6"/>
      <rect x="6" y="9" width="6" height="3" rx="0.6" fill={color}/>
      <circle cx="24" cy="14" r="1.5" fill={color}/>
    </svg>
  ),
  fileTile: ({size=28, color, label, theme}) => {
    return (
      <svg width={size} height={size} viewBox="0 0 28 28">
        <path d="M5 3h13l5 5v17a1 1 0 01-1 1H5a1 1 0 01-1-1V4a1 1 0 011-1z" fill={theme.panel} stroke={theme.line} strokeWidth="1"/>
        <path d="M18 3v5h5" fill={theme.sunken} stroke={theme.line} strokeWidth="1"/>
        <rect x="3" y="16" width="18" height="8" rx="1.4" fill={color}/>
        <text x="12" y="22" textAnchor="middle" fontSize="6.5" fontFamily="ui-monospace, SFMono-Regular, Menlo, monospace" fontWeight="700" fill="#fff" letterSpacing="0.5">{(label||'').toUpperCase()}</text>
      </svg>
    );
  },
};

// Map file `kind` → icon + accent color
const KIND_COLOR = {
  pdf:     '#c0432e',
  doc:     '#2d6dcf',
  sheet:   '#1f7a47',
  slide:   '#c75321',
  text:    '#6b6356',
  image:   '#7a4ac2',
  video:   '#b13a6d',
  audio:   '#1d8a8a',
  archive: '#806127',
  app:     '#2e8b5a',
};

function FileIcon({ item, size=28, theme }) {
  if (item.kind === 'folder' || item.kind === 'home' || item.kind === 'recent' || item.kind === 'star' || item.kind === 'download') {
    return <Icon.folder size={size} color={theme.mode==='dark'?'#d6a04a':'#e8b962'} back={theme.mode==='dark'?'#a07a32':'#caa052'} />;
  }
  if (item.kind === 'volume') return <Icon.volume size={size} color={theme.sub} />;
  const color = KIND_COLOR[item.kind] || theme.sub;
  return <Icon.fileTile size={size} color={color} label={item.ext} theme={theme} />;
}

window.Icon = Icon;
window.FileIcon = FileIcon;
window.KIND_COLOR = KIND_COLOR;
