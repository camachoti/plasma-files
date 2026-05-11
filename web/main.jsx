// Main app — assembles chrome + body into the prototype.
const { useState: ause, useMemo: aMemo, useEffect: aEffect } = React;

function FileManager() {
  const TWEAK_DEFAULTS = {"mode":"light","accent":"teal","density":"regular","view":"details","showRibbon":true,"showFooter":true};
  const [t, setTweak] = useTweaks(TWEAK_DEFAULTS);
  const theme = aMemo(() => ({ ...PlasmaData.makeTheme(t.mode, t.accent), mode: t.mode }), [t.mode, t.accent]);

  const [tabs, setTabs] = ause([
    { id: 1, path: '/storage/Documents' },
    { id: 2, path: '/storage/Downloads' },
  ]);
  const [activeTab, setActiveTab] = ause(1);
  const tab = tabs.find(x => x.id === activeTab) || tabs[0];

  const [histories, setHistories] = ause({ 1: { stack: ['/storage/Documents'], idx: 0 }, 2: { stack: ['/storage/Downloads'], idx: 0 } });
  const hist = histories[activeTab] || { stack: [tab.path], idx: 0 };

  const navTo = (path) => {
    setTabs(ts => ts.map(x => x.id === activeTab ? { ...x, path } : x));
    setHistories(hs => {
      const cur = hs[activeTab] || { stack: [], idx: -1 };
      const newStack = cur.stack.slice(0, cur.idx + 1).concat(path);
      return { ...hs, [activeTab]: { stack: newStack, idx: newStack.length - 1 } };
    });
    setSel(new Set());
  };
  const goBack = () => {
    const cur = histories[activeTab];
    if (cur.idx <= 0) return;
    const newIdx = cur.idx - 1;
    const path = cur.stack[newIdx];
    setTabs(ts => ts.map(x => x.id === activeTab ? { ...x, path } : x));
    setHistories(hs => ({ ...hs, [activeTab]: { ...cur, idx: newIdx } }));
    setSel(new Set());
  };
  const goForward = () => {
    const cur = histories[activeTab];
    if (cur.idx >= cur.stack.length - 1) return;
    const newIdx = cur.idx + 1;
    const path = cur.stack[newIdx];
    setTabs(ts => ts.map(x => x.id === activeTab ? { ...x, path } : x));
    setHistories(hs => ({ ...hs, [activeTab]: { ...cur, idx: newIdx } }));
    setSel(new Set());
  };
  const goUp = () => {
    if (tab.path === '/') return;
    const parts = tab.path.split('/').filter(Boolean);
    parts.pop();
    navTo(parts.length ? '/' + parts.join('/') : '/');
  };

  const closeTab = (id) => {
    if (tabs.length === 1) return;
    const newTabs = tabs.filter(x => x.id !== id);
    setTabs(newTabs);
    if (activeTab === id) setActiveTab(newTabs[0].id);
  };
  const newTab = () => {
    const id = Math.max(...tabs.map(x => x.id)) + 1;
    setTabs(ts => [...ts, { id, path: '/' }]);
    setHistories(hs => ({ ...hs, [id]: { stack: ['/'], idx: 0 } }));
    setActiveTab(id);
  };

  const [sel, setSel] = ause(new Set());
  const [clipboard, setClipboard] = ause(null);
  const [drawer, setDrawer] = ause(false);
  const [props, setProps] = ause(null);
  const [sortKey, setSortKey] = ause('name');
  const [sortDir, setSortDir] = ause('asc');
  const [searchOpen, setSearchOpen] = ause(false);
  const [query, setQuery] = ause('');
  const [toast, setToast] = ause(null);
  const showToast = (msg) => { setToast(msg); clearTimeout(showToast._t); showToast._t = setTimeout(() => setToast(null), 1800); };

  const folder = PlasmaData.FS[tab.path];
  let rawItems = folder ? folder.children : [];
  if (query) {
    const q = query.toLowerCase();
    rawItems = rawItems.filter(x => x.name.toLowerCase().includes(q));
  }
  const items = aMemo(() => {
    const f = rawItems.filter(x => x.kind === 'folder' || x.kind === 'volume');
    const fi = rawItems.filter(x => x.kind !== 'folder' && x.kind !== 'volume');
    const cmp = (a, b) => {
      if (sortKey === 'name') return a.name.localeCompare(b.name);
      if (sortKey === 'modified') return (a.modified || '').localeCompare(b.modified || '');
      if (sortKey === 'size') { const sa = parseFloat(a.size) || 0, sb = parseFloat(b.size) || 0; return sa - sb; }
      return 0;
    };
    f.sort(cmp); fi.sort(cmp);
    const all = [...f, ...fi];
    return sortDir === 'desc' ? all.reverse() : all;
  }, [rawItems, sortKey, sortDir]);

  const toggleSel = (name) => setSel(s => { const n = new Set(s); n.has(name) ? n.delete(name) : n.add(name); return n; });
  const toggleAll = () => setSel(s => s.size === items.length ? new Set() : new Set(items.map(x => x.name)));

  const onOpen = (item) => {
    if (item.kind === 'folder' || item.kind === 'volume') {
      navTo(item.path || (tab.path === '/' ? item.path : `${tab.path}/${item.name}`));
    } else {
      setProps(item);
    }
  };

  const ribbonItems = [
    { icon: <Icon.newfolder size={18} color={theme.text}/>, label: 'New', onClick: () => showToast('New folder') },
    { divider: true },
    { icon: <Icon.cut size={18} color={theme.text}/>, label: 'Cut', disabled: sel.size === 0, onClick: () => { setClipboard({op:'cut', items: [...sel]}); showToast(`Cut ${sel.size}`); } },
    { icon: <Icon.copy size={18} color={theme.text}/>, label: 'Copy', disabled: sel.size === 0, onClick: () => { setClipboard({op:'copy', items: [...sel]}); showToast(`Copied ${sel.size}`); } },
    { icon: <Icon.paste size={18} color={clipboard ? theme.text : theme.mute}/>, label: 'Paste', disabled: !clipboard, onClick: () => { showToast(`Pasted ${clipboard.items.length}`); setClipboard(null); } },
    { divider: true },
    { icon: <Icon.rename size={18} color={theme.text}/>, label: 'Rename', disabled: sel.size !== 1, onClick: () => showToast('Rename') },
    { icon: <Icon.share size={18} color={theme.text}/>, label: 'Share', disabled: sel.size === 0, onClick: () => showToast('Share sheet') },
    { icon: <Icon.trash size={18} color={sel.size > 0 ? theme.warn : theme.mute}/>, label: 'Delete', tone: sel.size > 0 ? 'warn' : null, disabled: sel.size === 0, onClick: () => { showToast(`Deleted ${sel.size}`); setSel(new Set()); } },
    { divider: true },
    { icon: <Icon.sort size={18} color={theme.text}/>, label: 'Sort', onClick: () => setSortDir(d => d === 'asc' ? 'desc' : 'asc') },
    { icon: <Icon.view size={18} color={theme.text}/>, label: 'View', onClick: () => setTweak('view', t.view === 'details' ? 'tiles' : 'details') },
    { icon: <Icon.info size={18} color={theme.text}/>, label: 'Details', disabled: sel.size === 0, onClick: () => { const first = items.find(x => sel.has(x.name)); if (first) setProps(first); } },
  ];

  const titleForPath = (p) => {
    if (p === '/') return 'Home';
    const last = p.split('/').filter(Boolean).pop();
    return last === 'storage' ? 'Internal storage' : last === 'sdcard' ? 'SD card' : last;
  };

  return (
    <div style={{
      width: 412, height: 892, borderRadius: 36, overflow: 'hidden',
      background: theme.bg, border: `9px solid #1a1a1a`,
      boxShadow: '0 30px 80px rgba(0,0,0,0.25)',
      display: 'flex', flexDirection: 'column', boxSizing: 'border-box',
      position: 'relative',
    }}>
      <Chrome.StatusBar theme={theme}/>
      <Chrome.TabStrip
        tabs={tabs.map(x => ({ id: x.id, title: titleForPath(x.path) }))}
        activeId={activeTab} onSelect={setActiveTab} onClose={closeTab} onNew={newTab} theme={theme}
      />
      <Chrome.PathBar
        path={tab.path} history={hist.stack} hIdx={hist.idx}
        onNav={navTo} onBack={goBack} onForward={goForward} onUp={goUp}
        onMenu={() => setDrawer(true)} onSearch={() => setSearchOpen(s => !s)} theme={theme}
      />
      {searchOpen && (
        <div style={{ padding: '8px 10px', background: theme.panel, borderBottom: `1px solid ${theme.line}`, display: 'flex', alignItems: 'center', gap: 8 }}>
          <Icon.search size={16} color={theme.sub}/>
          <input autoFocus value={query} onChange={e => setQuery(e.target.value)} placeholder="Search this folder…"
            style={{ flex: 1, border: 'none', outline: 'none', background: 'transparent', fontSize: 14, color: theme.text, fontFamily: 'system-ui, sans-serif' }}/>
          {query && <button onClick={() => setQuery('')} style={{border:'none', background:'transparent', cursor:'pointer'}}><Icon.close size={14} color={theme.sub}/></button>}
        </div>
      )}
      {t.showRibbon && (sel.size === 0
        ? <Chrome.Ribbon items={ribbonItems} theme={theme}/>
        : <Chrome.SelectionBar
            count={sel.size} onCancel={() => setSel(new Set())}
            onCopy={() => { setClipboard({op:'copy', items: [...sel]}); showToast(`Copied ${sel.size}`); setSel(new Set()); }}
            onCut={() => { setClipboard({op:'cut', items: [...sel]}); showToast(`Cut ${sel.size}`); setSel(new Set()); }}
            onShare={() => showToast('Share sheet')}
            onDelete={() => { showToast(`Deleted ${sel.size}`); setSel(new Set()); }}
            onMore={() => showToast('More options')} theme={theme}
          />
      )}
      <div style={{flex: 1, overflowY: 'auto', background: theme.bg}}>
        {t.view === 'details' && (
          <>
            <Body.ColHeader
              sortKey={sortKey} sortDir={sortDir}
              onSort={(k) => { if (sortKey === k) setSortDir(d => d === 'asc' ? 'desc' : 'asc'); else { setSortKey(k); setSortDir('asc'); } }}
              density={t.density} theme={theme}
              hasSelection={sel.size > 0}
              allChecked={sel.size === items.length && items.length > 0}
              onToggleAll={toggleAll}
            />
            {items.length === 0
              ? <Body.EmptyState theme={theme} label={query ? `No matches for "${query}"` : 'This folder is empty'}/>
              : items.map(it => (
                  <Body.ListRow key={it.name} item={it} selected={sel.has(it.name)} density={t.density}
                    theme={theme} onOpen={() => onOpen(it)} onToggle={() => toggleSel(it.name)}
                    onLongPress={() => toggleSel(it.name)} anySelected={sel.size > 0}
                  />
                ))}
          </>
        )}
        {t.view === 'tiles' && (
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: 8, padding: 10 }}>
            {items.map(it => (
              <Body.GridTile key={it.name} item={it} selected={sel.has(it.name)}
                theme={theme} onOpen={() => onOpen(it)} onToggle={() => toggleSel(it.name)}
                onLongPress={() => toggleSel(it.name)} anySelected={sel.size > 0}
              />
            ))}
          </div>
        )}
      </div>
      {t.showFooter && <Chrome.StatusFooter count={items.length} sel={sel.size} freeText="56.8 GB free" theme={theme}/>}
      <div style={{height: 22, display: 'flex', alignItems: 'center', justifyContent: 'center', background: theme.bg}}>
        <div style={{width: 108, height: 4, borderRadius: 2, background: theme.text, opacity: 0.35}}/>
      </div>
      <Body.Drawer open={drawer} onClose={() => setDrawer(false)} currentPath={tab.path} onNav={navTo} theme={theme}/>
      <Body.PropsSheet item={props} theme={theme} onClose={() => setProps(null)}/>
      {toast && (
        <div style={{ position: 'absolute', bottom: 80, left: '50%', transform: 'translateX(-50%)', background: theme.text, color: theme.bg, padding: '8px 14px', borderRadius: 8, fontSize: 12.5, fontWeight: 500, zIndex: 30, fontFamily: 'system-ui, sans-serif', boxShadow: '0 4px 18px rgba(0,0,0,0.25)' }}>{toast}</div>
      )}
      <TweaksPanel>
        <TweakSection label="Appearance"/>
        <TweakRadio label="Theme" value={t.mode} options={[{value:'light',label:'Light'},{value:'dark',label:'Dark'}]} onChange={v => setTweak('mode', v)}/>
        <TweakRadio label="Accent" value={t.accent} options={[{value:'teal',label:'Teal'},{value:'amber',label:'Amber'},{value:'violet',label:'Violet'},{value:'rose',label:'Rose'}]} onChange={v => setTweak('accent', v)}/>
        <TweakSection label="Layout"/>
        <TweakRadio label="View" value={t.view} options={[{value:'details',label:'Details'},{value:'tiles',label:'Tiles'}]} onChange={v => setTweak('view', v)}/>
        <TweakRadio label="Density" value={t.density} options={[{value:'compact',label:'Compact'},{value:'cozy',label:'Cozy'},{value:'regular',label:'Regular'}]} onChange={v => setTweak('density', v)}/>
        <TweakToggle label="Show ribbon" value={t.showRibbon} onChange={v => setTweak('showRibbon', v)}/>
        <TweakToggle label="Status footer" value={t.showFooter} onChange={v => setTweak('showFooter', v)}/>
      </TweaksPanel>
    </div>
  );
}

function App() {
  const [scale, setScale] = ause(1);
  aEffect(() => {
    const FRAME_W = 412, FRAME_H = 892, PAD = 24;
    const fit = () => {
      const sw = window.innerWidth - PAD * 2;
      const sh = window.innerHeight - PAD * 2;
      const s = Math.min(sw / FRAME_W, sh / FRAME_H, 1);
      setScale(s);
    };
    fit();
    window.addEventListener('resize', fit);
    return () => window.removeEventListener('resize', fit);
  }, []);
  return (
    <div style={{ minHeight: '100vh', width: '100%', display: 'flex', alignItems: 'center', justifyContent: 'center', background: 'radial-gradient(circle at 50% 30%, #2c2a26 0%, #0f0e0c 90%)', padding: 24, boxSizing: 'border-box', overflow: 'hidden' }}>
      <div style={{ width: 412 * scale, height: 892 * scale, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
        <div style={{ transform: `scale(${scale})`, transformOrigin: 'center center', flexShrink: 0 }}>
          <FileManager/>
        </div>
      </div>
    </div>
  );
}

ReactDOM.createRoot(document.getElementById('root')).render(<App/>);
