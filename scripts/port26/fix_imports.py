"""Rewrite imports of classes that moved in 26.3, by simple-name lookup in the MC + NeoForge sources.
usage: fiximports.py <src_root> <srcdir1> <srcdir2> ...   (prints what it changed and what it could not place)"""
import os, re, sys
root = sys.argv[1]; srcs = sys.argv[2:]
index = {}  # simple name -> set of fqcn (top-level and nested)
exists = set()
for s in srcs:
    for dp, _, fs in os.walk(s):
        for f in fs:
            if not f.endswith('.java'): continue
            rel = os.path.relpath(os.path.join(dp, f), s)[:-5].replace('/', '.')
            exists.add(rel)
            index.setdefault(rel.rsplit('.', 1)[-1], set()).add(rel)
            # nested public types
            try: txt = open(os.path.join(dp, f), encoding='utf-8').read()
            except Exception: continue
            for m in re.finditer(r'^\s{4}(?:public |protected )?(?:static )?(?:final |abstract |sealed )*(?:class|interface|enum|record) (\w+)', txt, re.M):
                n = rel + '.' + m.group(1); exists.add(n); index.setdefault(m.group(1), set()).add(n)
RENAMES = {'ResourceLocation': 'net.minecraft.resources.Identifier', 'GuiGraphics': 'net.minecraft.client.gui.GuiGraphicsExtractor',
           'ClickType': 'net.minecraft.world.inventory.ContainerInput',
           'ItemInHandRenderer': 'net.minecraft.client.renderer.FirstPersonHandsAndItemsRenderer',
           'ResourceLocationArgument': 'net.minecraft.commands.arguments.IdentifierArgument',
           'MobSpawnType': 'net.minecraft.world.entity.EntitySpawnReason',
           'UseAnim': 'net.minecraft.world.item.ItemUseAnimation'}
unplaced = set()
for dp, _, fs in os.walk(root):
    for f in fs:
        if not f.endswith('.java'): continue
        p = os.path.join(dp, f); txt = open(p, encoding='utf-8').read(); orig = txt
        def fix(m):
            static, fq = m.group(1) or '', m.group(2)
            if not (fq.startswith('net.minecraft') or fq.startswith('net.neoforged') or fq.startswith('com.mojang')): return m.group(0)
            cls = fq
            member = None
            if static:
                cls, member = fq.rsplit('.', 1)
            if cls in exists: return m.group(0)
            simple = cls.rsplit('.', 1)[-1]
            if simple in RENAMES:
                new = RENAMES[simple]
            else:
                cands = sorted(index.get(simple, ()))
                cands = [c for c in cands if '.critereon.' not in c]
                if len(cands) == 1: new = cands[0]
                else:
                    unplaced.add(f'{fq} -> {cands[:6]}'); return m.group(0)
            return f'import {static}{new}{"." + member if member else ""};'
        txt = re.sub(r'import (static )?([\w.]+);', fix, txt)
        for old, new in RENAMES.items():
            txt = re.sub(r'\b' + old + r'\b', new.rsplit('.', 1)[-1], txt)
        if txt != orig: open(p, 'w', encoding='utf-8').write(txt); print('fixed', os.path.relpath(p, root))
for u in sorted(unplaced): print('UNPLACED', u)
