// Chrome — top bar, tabs, breadcrumb, ribbon toolbar
const { useState: cuseState, useRef: cuseRef, useEffect: cuseEffect } = React;

function StatusBar({ theme }) {
  return (
    <div style={{
      height: 30, padding: '0 18px', display: 'flex', alignItems: 'center',
      justifyContent: 'space-between', color: theme.text, fontSize: 13,
      fontWeight: 600, letterSpacing: 0.1, position: 'relative',
      fontFamily: 'system-ui, -apple-system, "Segoe UI", Roboto, sans-serif',
    }}>
      <span>9:30</span>
      <div style={{
        position: 'absolute', left: '50%', top: 6, transform: 'translateX(-50%)',
        width: 18, height: 18, borderRadius: '50%', background: '#2e2e2e',
      }} />
      <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
        <svg width="14" height="14" viewBox="0 0 16 16"><path d="M8 13L1 6a10 10 0 0114 0z" fill={theme.text}/></svg>
        <svg width="14" height="14" viewBox="0 0 16 16"><path d="M14 14V2L2 14z" fill={theme.text}/></svg>
        <span style={{fontSize: 11, fontWeight: 700}}>72%</span>
        <svg width="20" height="11" viewBox="0 0 22 11">
          <rect x="0.5" y="0.5" width="19" height="10" rx="2.2" fill="none" stroke={theme.text} strokeWidth="1"/>
          <rect x="2" y="2" width="14" height="7" rx="1" fill={theme.text}/>
          <rect x="20" y="3.5" width="1.5" height="4" rx="0.5" fill={theme.text}/>
        </svg>
      </div>
    </div>
  );
}

function TabStrip({ tabs, activeId, onSelect, onClose, onNew, theme }) {
  return (
    <div style={{
      display: 'flex', alignItems: 'flex-end', gap: 2,
      padding: '6px 8px 0', background: theme.bg,
      borderBottom: `1px solid ${theme.line}`,
      fontFamily: 'system-ui, -apple-system, "Segoe UI", Roboto, sans-serif',
      overflowX: 'auto', scrollbarWidth: 'none',
    }}>
      {tabs.map(t => {
        const active = t.id === activeId;
        return (
          <div key={t.id}
            onClick={() => onSelect(t.id)}
            style={{
              display: 'flex', alignItems: 'center', gap: 8,
              padding: '6px 8px 7px 10px',
              background: active ? theme.panel : 'transparent',
              borderRadius: '10px 10px 0 0',
              border: active ? `1px solid ${theme.line}` : '1px solid transparent',
              borderBottom: active ? `1px solid ${theme.panel}` : '1px solid transparent',
              marginBottom: -1,
              fontSize: 12.5, color: active ? theme.text : theme.sub,
              fontWeight: active ? 600 : 500,
              maxWidth: 160, minWidth: 90, flexShrink: 0,
            }}>
            <Icon.folder size={14} color={theme.mode==='dark'?'#d6a04a':'#e8b962'} back={theme.mode==='dark'?'#a07a32':'#caa052'} />
            <span style={{whiteSpace:'nowrap', overflow:'hidden', textOverflow:'ellipsis', flex: 1}}>{t.title}</span>
            <span
              onClick={(e) => { e.stopPropagation(); onClose(t.id); }}
              style={{ width: 18, height: 18, display: 'flex', alignItems: 'center', justifyContent: 'center', borderRadius: 4, opacity: active ? 1 : 0.55 }}>
              <Icon.close size={12} color={theme.sub}/>
            </span>
          </div>
        );
      })}
      <button onClick={onNew} style={{
        background: 'none', border: 'none', padding: '6px 8px',
        cursor: 'pointer', display: 'flex', alignItems: 'center',
        color: theme.sub, marginBottom: 2,
      }}>
        <Icon.plus size={16} color={theme.sub}/>
      </button>
    </div>
  );
}

function PathBar({ path, history, hIdx, onNav, onBack, onForward, onUp, onMenu, onSearch, theme }) {
  const segs = path === '/' ? [{ label: 'Home', path: '/' }] :
    [{ label: 'Home', path: '/' }].concat(
      path.split('/').filter(Boolean).map((s, i, arr) => ({
        label: s === 'storage' ? 'Internal storage' : s === 'sdcard' ? 'SD card' : s,
        path: '/' + arr.slice(0, i+1).join('/'),
      }))
    );
  const scrollerRef = cuseRef(null);
  cuseEffect(() => {
    if (scrollerRef.current) scrollerRef.current.scrollLeft = scrollerRef.current.scrollWidth;
  }, [path]);
  const navBtn = (icon, onClick, disabled, key) => (
    <button key={key} onClick={onClick} disabled={disabled} style={{
      width: 32, height: 32, border: 'none', background: 'transparent',
      borderRadius: 7, cursor: disabled ? 'default' : 'pointer',
      display: 'flex', alignItems: 'center', justifyContent: 'center',
      opacity: disabled ? 0.3 : 1, color: theme.text, flexShrink: 0,
    }}>{icon}</button>
  );
  return (
    <div style={{ background: theme.panel, borderBottom: `1px solid ${theme.line}`, fontFamily: 'system-ui, -apple-system, "Segoe UI", Roboto, sans-serif' }}>
      <div style={{ display: 'flex', alignItems: 'center', gap: 2, padding: '4px 6px' }}>
        {navBtn(<Icon.back size={18} color={theme.text}/>, onBack, hIdx === 0, 'b')}
        {navBtn(<Icon.forward size={18} color={theme.text}/>, onForward, hIdx >= history.length - 1, 'f')}
        {navBtn(<Icon.up size={18} color={theme.text}/>, onUp, path === '/', 'u')}
        <div style={{flex: 1}}/>
        <div style={{ fontSize: 12.5, color: theme.sub, fontWeight: 500, padding: '0 8px', whiteSpace: 'nowrap' }}>{segs[segs.length-1].label}</div>
        <div style={{flex: 1}}/>
        {navBtn(<Icon.search size={18} color={theme.text}/>, onSearch, false, 's')}
        {navBtn(<Icon.menu size={18} color={theme.text}/>, onMenu, false, 'm')}
      </div>
      <div ref={scrollerRef} style={{
        margin: '0 8px 6px', display: 'flex', alignItems: 'center',
        background: theme.sunken, borderRadius: 7, border: `1px solid ${theme.softLine}`,
        height: 30, padding: '0 6px', gap: 2,
        overflowX: 'auto', scrollbarWidth: 'none', whiteSpace: 'nowrap',
      }}>
        {segs.map((s, i) => (
          <React.Fragment key={i}>
            <span onClick={() => onNav(s.path)} style={{
              fontSize: 13, color: i === segs.length-1 ? theme.text : theme.sub,
              fontWeight: i === segs.length-1 ? 600 : 500,
              padding: '4px 7px', borderRadius: 5, cursor: 'pointer', flexShrink: 0,
            }}>{s.label}</span>
            {i < segs.length-1 && (
              <span style={{color: theme.mute, display:'flex', alignItems:'center', flexShrink: 0}}>
                <Icon.chevron size={12} color={theme.mute}/>
              </span>
            )}
          </React.Fragment>
        ))}
      </div>
    </div>
  );
}

function Ribbon({ items, theme }) {
  return (
    <div style={{
      display: 'flex', gap: 2, padding: '6px 4px',
      background: theme.panel, borderBottom: `1px solid ${theme.line}`,
      overflowX: 'auto', scrollbarWidth: 'none',
      fontFamily: 'system-ui, -apple-system, "Segoe UI", Roboto, sans-serif',
    }}>
      {items.map((it, i) => it.divider ? (
        <div key={i} style={{width: 1, background: theme.line, margin: '2px 4px'}}/>
      ) : (
        <button key={i} onClick={it.onClick} disabled={it.disabled} style={{
          display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 3,
          padding: '6px 10px', minWidth: 56, background: it.active ? theme.accentSoft : 'transparent',
          border: 'none', borderRadius: 7, cursor: it.disabled ? 'default' : 'pointer',
          color: it.disabled ? theme.mute : (it.tone === 'warn' ? theme.warn : theme.text),
          flexShrink: 0,
        }}>
          {it.icon}
          <span style={{fontSize: 10.5, fontWeight: 500, letterSpacing: 0.1}}>{it.label}</span>
        </button>
      ))}
    </div>
  );
}

function SelectionBar({ count, onCancel, onCopy, onCut, onShare, onDelete, onMore, theme }) {
  const selBtn = { width: 34, height: 34, border: 'none', background: 'transparent', borderRadius: 7,
    display: 'flex', alignItems: 'center', justifyContent: 'center', cursor: 'pointer' };
  return (
    <div style={{
      display: 'flex', alignItems: 'center', gap: 4,
      padding: '6px 8px', background: theme.accent, color: '#fff',
      fontFamily: 'system-ui, -apple-system, "Segoe UI", Roboto, sans-serif',
    }}>
      <button onClick={onCancel} style={{...selBtn, width: 34, height: 34}}><Icon.close size={18} color="#fff"/></button>
      <span style={{flex:1, fontSize: 14, fontWeight: 600}}>{count} selected</span>
      <button onClick={onCopy}   style={selBtn}><Icon.copy size={18} color="#fff"/></button>
      <button onClick={onCut}    style={selBtn}><Icon.cut size={18} color="#fff"/></button>
      <button onClick={onShare}  style={selBtn}><Icon.share size={18} color="#fff"/></button>
      <button onClick={onDelete} style={selBtn}><Icon.trash size={18} color="#fff"/></button>
      <button onClick={onMore}   style={selBtn}><Icon.more size={18} color="#fff"/></button>
    </div>
  );
}

function StatusFooter({ count, sel, freeText, theme }) {
  return (
    <div style={{
      display: 'flex', alignItems: 'center', gap: 12,
      padding: '0 12px', height: 28,
      background: theme.panel, borderTop: `1px solid ${theme.line}`,
      color: theme.sub, fontSize: 11.5, fontWeight: 500,
      fontFamily: 'system-ui, -apple-system, "Segoe UI", Roboto, sans-serif',
    }}>
      <span>{count} items{sel ? ` · ${sel} selected` : ''}</span>
      <span style={{flex:1}}/>
      <span>{freeText}</span>
    </div>
  );
}

window.Chrome = { StatusBar, TabStrip, PathBar, Ribbon, SelectionBar, StatusFooter };
