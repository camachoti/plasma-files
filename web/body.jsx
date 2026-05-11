// Body components — file list, drawer, properties sheet
const { useState: buseState, useMemo: buseMemo, useRef: buseRef } = React;

function Check({ checked, indeterminate, theme, onClick, size=18 }) {
  return (
    <div onClick={(e) => { e.stopPropagation(); onClick && onClick(e); }} style={{
      width: size, height: size, borderRadius: 4,
      border: `1.5px solid ${checked ? theme.accent : theme.mute}`,
      background: checked ? theme.accent : 'transparent',
      display: 'flex', alignItems: 'center', justifyContent: 'center',
      flexShrink: 0, cursor: 'pointer',
    }}>
      {checked && !indeterminate && <Icon.check size={size-6} color="#fff"/>}
      {indeterminate && <div style={{width:8, height:2, background:'#fff', borderRadius:1}}/>}
    </div>
  );
}

function ColHeader({ sortKey, sortDir, onSort, density, theme, hasSelection, allChecked, onToggleAll }) {
  const h = density === 'compact' ? 26 : density === 'cozy' ? 28 : 32;
  const cell = (key, label, w, align='left') => (
    <div onClick={() => onSort(key)} style={{
      flex: w === 'flex' ? 1 : `0 0 ${w}px`, fontSize: 11, fontWeight: 600,
      color: theme.sub, textTransform: 'uppercase', letterSpacing: 0.4,
      display: 'flex', alignItems: 'center', justifyContent: align === 'right' ? 'flex-end' : 'flex-start',
      gap: 4, cursor: 'pointer', userSelect: 'none',
    }}>
      <span>{label}</span>
      {sortKey === key && <Icon.chevron size={10} color={theme.sub} dir={sortDir === 'asc' ? 'up' : 'down'}/>}
    </div>
  );
  return (
    <div style={{
      display: 'flex', alignItems: 'center', gap: 10, height: h,
      padding: '0 12px', borderBottom: `1px solid ${theme.line}`, background: theme.bg,
    }}>
      <div style={{width: 22, display: 'flex', alignItems: 'center'}}>
        <Check checked={allChecked} indeterminate={hasSelection && !allChecked} theme={theme} onClick={onToggleAll}/>
      </div>
      <div style={{width: 28}}/>
      {cell('name', 'Name', 'flex')}
      {cell('modified', 'Date modified', 110)}
      {cell('size', 'Size', 60, 'right')}
    </div>
  );
}

function ListRow({ item, selected, density, theme, onOpen, onToggle, onLongPress, anySelected }) {
  const h = density === 'compact' ? 36 : density === 'cozy' ? 44 : 52;
  const iconSize = density === 'compact' ? 22 : density === 'cozy' ? 26 : 30;
  const fontSize = density === 'compact' ? 13 : 14;
  const lpTimer = buseRef(null);
  const startLP = () => { lpTimer.current = setTimeout(() => onLongPress && onLongPress(), 380); };
  const cancelLP = () => clearTimeout(lpTimer.current);
  return (
    <div
      onClick={() => anySelected ? onToggle() : onOpen()}
      onMouseDown={startLP} onMouseUp={cancelLP} onMouseLeave={cancelLP}
      onTouchStart={startLP} onTouchEnd={cancelLP}
      style={{
        display: 'flex', alignItems: 'center', gap: 10, height: h,
        padding: '0 12px',
        background: selected ? theme.selected : 'transparent',
        borderLeft: selected ? `2.5px solid ${theme.accent}` : '2.5px solid transparent',
        borderBottom: `1px solid ${theme.softLine}`,
        cursor: 'pointer',
        fontFamily: 'system-ui, -apple-system, "Segoe UI", Roboto, sans-serif',
      }}>
      <div style={{width: 22, display: 'flex', alignItems: 'center'}}>
        {anySelected
          ? <Check checked={selected} theme={theme} onClick={onToggle}/>
          : <div style={{width: 18, height: 18, opacity: 0}}/>}
      </div>
      <div style={{width: 28, display: 'flex', alignItems: 'center', justifyContent: 'center'}}>
        <FileIcon item={item} size={iconSize} theme={theme}/>
      </div>
      <div style={{flex: 1, minWidth: 0, display: 'flex', flexDirection: 'column', justifyContent: 'center'}}>
        <div style={{ fontSize, color: theme.text, fontWeight: 500, whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>{item.name}</div>
        {density !== 'compact' && (
          <div style={{fontSize: 11.5, color: theme.sub, marginTop: 1}}>
            {item.kind === 'folder' ? `${item.items} items` : item.kind === 'volume' ? `${item.used} of ${item.total} GB used` : (item.ext ? item.ext.toUpperCase() + ' file' : '')}
          </div>
        )}
      </div>
      <div style={{flex: '0 0 110px', fontSize: 12, color: theme.sub, whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis'}}>{item.modified || '—'}</div>
      <div style={{flex: '0 0 60px', fontSize: 12, color: theme.sub, textAlign: 'right', fontVariantNumeric: 'tabular-nums'}}>{item.size || (item.kind === 'folder' ? '—' : '')}</div>
    </div>
  );
}

function GridTile({ item, selected, theme, onOpen, onToggle, anySelected, onLongPress }) {
  const lpTimer = buseRef(null);
  const startLP = () => { lpTimer.current = setTimeout(() => onLongPress && onLongPress(), 380); };
  const cancelLP = () => clearTimeout(lpTimer.current);
  return (
    <div
      onClick={() => anySelected ? onToggle() : onOpen()}
      onMouseDown={startLP} onMouseUp={cancelLP} onMouseLeave={cancelLP}
      onTouchStart={startLP} onTouchEnd={cancelLP}
      style={{
        position: 'relative', padding: '10px 6px',
        background: selected ? theme.selected : theme.panel,
        border: `1px solid ${selected ? theme.accent : theme.softLine}`,
        borderRadius: 8,
        display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 6,
        cursor: 'pointer',
        fontFamily: 'system-ui, -apple-system, "Segoe UI", Roboto, sans-serif',
      }}>
      {anySelected && (
        <div style={{position:'absolute', top:6, left:6}}>
          <Check checked={selected} theme={theme} onClick={onToggle} size={16}/>
        </div>
      )}
      <FileIcon item={item} size={42} theme={theme}/>
      <div style={{ fontSize: 12, color: theme.text, fontWeight: 500, textAlign: 'center', width: '100%', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap', padding: '0 4px' }}>{item.name}</div>
      <div style={{fontSize: 10.5, color: theme.sub}}>{item.kind === 'folder' ? `${item.items} items` : item.size || ''}</div>
    </div>
  );
}

function Drawer({ open, onClose, currentPath, onNav, theme }) {
  return (
    <>
      <div onClick={onClose} style={{
        position: 'absolute', inset: 0, background: 'rgba(0,0,0,0.35)',
        opacity: open ? 1 : 0, pointerEvents: open ? 'auto' : 'none',
        transition: 'opacity 0.18s ease', zIndex: 20,
      }}/>
      <div style={{
        position: 'absolute', top: 0, bottom: 0, left: 0, width: 268,
        background: theme.panel, borderRight: `1px solid ${theme.line}`,
        transform: `translateX(${open ? '0' : '-100%'})`,
        transition: 'transform 0.22s cubic-bezier(.2,.7,.3,1)',
        zIndex: 21, overflowY: 'auto',
        fontFamily: 'system-ui, -apple-system, "Segoe UI", Roboto, sans-serif',
      }}>
        <div style={{ padding: '14px 16px 12px', display: 'flex', alignItems: 'center', gap: 10, borderBottom: `1px solid ${theme.line}` }}>
          <div style={{ width: 30, height: 30, borderRadius: 8, background: theme.accent, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
            <svg width="18" height="18" viewBox="0 0 18 18"><path d="M2 5a1 1 0 011-1h4l1.5 1.5H15a1 1 0 011 1V14a1 1 0 01-1 1H3a1 1 0 01-1-1z" fill="#fff"/></svg>
          </div>
          <div>
            <div style={{fontSize: 15, fontWeight: 700, color: theme.text, letterSpacing: -0.1}}>Plasma Files</div>
            <div style={{fontSize: 11, color: theme.sub}}>71.2 GB used of 128 GB</div>
          </div>
        </div>
        <div style={{padding: '12px 16px'}}>
          <div style={{height: 6, borderRadius: 3, background: theme.sunken, overflow: 'hidden', display: 'flex'}}>
            <div style={{width: '36%', background: KIND_COLOR.image}}/>
            <div style={{width: '12%', background: KIND_COLOR.video}}/>
            <div style={{width: '6%', background: KIND_COLOR.audio}}/>
            <div style={{width: '4%', background: KIND_COLOR.doc}}/>
            <div style={{width: '8%', background: theme.sub}}/>
          </div>
          <div style={{display:'flex', gap: 8, marginTop: 8, flexWrap: 'wrap', fontSize: 10.5, color: theme.sub}}>
            {[['image','Photos 24.6 GB'],['video','Video 8.1 GB'],['audio','Audio 4.0 GB'],['doc','Docs 2.7 GB']].map(([k,l]) => (
              <div key={k} style={{display:'flex', alignItems:'center', gap: 4}}>
                <div style={{width: 8, height: 8, borderRadius: 2, background: KIND_COLOR[k]}}/>
                <span>{l}</span>
              </div>
            ))}
          </div>
        </div>
        {PlasmaData.SIDEBAR.map(group => (
          <div key={group.group} style={{padding: '4px 8px 8px'}}>
            <div style={{ fontSize: 10.5, fontWeight: 700, color: theme.mute, letterSpacing: 0.6, textTransform: 'uppercase', padding: '8px 10px 4px' }}>{group.group}</div>
            {group.items.map(it => {
              const active = currentPath === it.path;
              return (
                <div key={it.name} onClick={() => { onNav(it.path); onClose(); }} style={{
                  display: 'flex', alignItems: 'center', gap: 10, padding: '8px 10px',
                  borderRadius: 6, cursor: 'pointer',
                  background: active ? theme.accentSoft : 'transparent',
                  color: active ? theme.accent : theme.text,
                  fontWeight: active ? 600 : 500, fontSize: 13.5,
                }}>
                  <div style={{width: 22, display: 'flex', justifyContent: 'center'}}>
                    {it.kind === 'volume'
                      ? <Icon.volume size={18} color={active ? theme.accent : theme.sub}/>
                      : it.kind === 'star'   ? <Icon.star size={18} color={active ? theme.accent : theme.sub} filled/>
                      : it.kind === 'recent' ? <svg width="18" height="18" viewBox="0 0 18 18"><circle cx="9" cy="9" r="7" fill="none" stroke={active ? theme.accent : theme.sub} strokeWidth="1.5"/><path d="M9 5v4l3 2" fill="none" stroke={active ? theme.accent : theme.sub} strokeWidth="1.5" strokeLinecap="round"/></svg>
                      : <Icon.folder size={20} color={theme.mode==='dark'?'#d6a04a':'#e8b962'} back={theme.mode==='dark'?'#a07a32':'#caa052'}/>}
                  </div>
                  <span style={{flex: 1, whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis'}}>{it.name}</span>
                  {it.meta && <span style={{fontSize: 11, color: theme.sub, fontWeight: 500}}>{it.meta}</span>}
                </div>
              );
            })}
          </div>
        ))}
        <div style={{height: 1, background: theme.line, margin: '4px 12px'}}/>
        <div style={{padding: '6px 8px 18px'}}>
          {['Trash', 'Cloud accounts', 'Settings'].map(t => (
            <div key={t} style={{padding: '8px 10px', fontSize: 13, color: theme.text, cursor: 'pointer'}}>{t}</div>
          ))}
        </div>
      </div>
    </>
  );
}

function PropsSheet({ item, theme, onClose }) {
  if (!item) return null;
  return (
    <>
      <div onClick={onClose} style={{ position: 'absolute', inset: 0, background: 'rgba(0,0,0,0.4)', zIndex: 25 }}/>
      <div style={{
        position: 'absolute', left: 0, right: 0, bottom: 0,
        background: theme.panel, borderTop: `1px solid ${theme.line}`,
        borderRadius: '16px 16px 0 0',
        padding: '8px 0 16px',
        zIndex: 26, maxHeight: '70%', overflowY: 'auto',
        fontFamily: 'system-ui, -apple-system, "Segoe UI", Roboto, sans-serif',
      }}>
        <div style={{width: 36, height: 4, background: theme.line, borderRadius: 2, margin: '4px auto 14px'}}/>
        <div style={{display: 'flex', alignItems: 'center', gap: 14, padding: '0 18px'}}>
          <FileIcon item={item} size={46} theme={theme}/>
          <div style={{flex: 1, minWidth: 0}}>
            <div style={{fontSize: 16, fontWeight: 600, color: theme.text, wordBreak: 'break-word'}}>{item.name}</div>
            <div style={{fontSize: 12, color: theme.sub, marginTop: 2}}>
              {item.kind === 'folder' ? `Folder · ${item.items} items` : `${(item.ext||'').toUpperCase()} · ${item.size||''}`}
            </div>
          </div>
          <button onClick={onClose} style={{ width: 32, height: 32, border: 'none', background: theme.sunken, borderRadius: 8, display: 'flex', alignItems: 'center', justifyContent: 'center', cursor: 'pointer' }}><Icon.close size={16} color={theme.text}/></button>
        </div>
        <div style={{display: 'flex', gap: 8, padding: '14px 18px 4px'}}>
          {[
            { icon: <Icon.share size={16} color={theme.text}/>, label: 'Share' },
            { icon: <Icon.copy  size={16} color={theme.text}/>, label: 'Copy' },
            { icon: <Icon.rename size={16} color={theme.text}/>, label: 'Rename' },
            { icon: <Icon.star  size={16} color={theme.text}/>, label: 'Star' },
            { icon: <Icon.trash size={16} color={theme.warn}/>, label: 'Delete', warn: true },
          ].map(b => (
            <div key={b.label} style={{
              flex: 1, padding: '10px 4px', textAlign: 'center',
              background: theme.sunken, borderRadius: 8,
              fontSize: 11, color: b.warn ? theme.warn : theme.text, fontWeight: 500,
              display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 4,
            }}>
              {b.icon}<span>{b.label}</span>
            </div>
          ))}
        </div>
        <div style={{ margin: '14px 18px 0', background: theme.sunken, borderRadius: 10, padding: '4px 0', border: `1px solid ${theme.softLine}` }}>
          {[
            ['Type',         item.kind === 'folder' ? 'File folder' : `${(item.ext||'').toUpperCase()} document`],
            ['Location',     item.parent || '/storage/Documents'],
            ['Size',         item.size || '—'],
            ['Modified',     item.modified || '—'],
            ['Permissions',  'Read · Write'],
          ].map(([k,v], i, a) => (
            <div key={k} style={{ display: 'flex', padding: '8px 14px', borderBottom: i < a.length-1 ? `1px solid ${theme.softLine}` : 'none', fontSize: 12.5 }}>
              <div style={{width: 100, color: theme.sub, fontWeight: 500}}>{k}</div>
              <div style={{flex: 1, color: theme.text, fontFamily: k === 'Location' ? 'ui-monospace, monospace' : 'inherit', wordBreak: 'break-word'}}>{v}</div>
            </div>
          ))}
        </div>
      </div>
    </>
  );
}

function EmptyState({ theme, label='This folder is empty' }) {
  return (
    <div style={{ padding: '60px 30px', textAlign: 'center', color: theme.sub, fontSize: 13, fontFamily: 'system-ui, sans-serif' }}>
      <div style={{margin: '0 auto 14px', opacity: 0.5}}>
        <Icon.folder size={56} color={theme.mute} back={theme.line}/>
      </div>
      {label}
    </div>
  );
}

window.Body = { Check, ColHeader, ListRow, GridTile, Drawer, PropsSheet, EmptyState };
