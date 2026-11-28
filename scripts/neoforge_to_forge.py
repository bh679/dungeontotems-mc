#!/usr/bin/env python3
"""Mechanical NeoForge 21.1 -> Forge 47 renames for Java sources (imports and simple names).

Only the renames that are always right. API differences (attachments, payloads, data maps,
tick event phases, damage events) are ported by hand afterwards.
Usage: scripts/neoforge_to_forge.py <file-or-dir>...
"""
import pathlib
import re
import sys

IMPORTS = [
    ("net.neoforged.bus.api.", "net.minecraftforge.eventbus.api."),
    ("net.neoforged.fml.common.EventBusSubscriber", "net.minecraftforge.fml.common.Mod.EventBusSubscriber"),
    ("net.neoforged.fml.", "net.minecraftforge.fml."),
    ("net.neoforged.api.distmarker.", "net.minecraftforge.api.distmarker."),
    ("net.neoforged.neoforge.common.NeoForge;", "net.minecraftforge.common.MinecraftForge;"),
    ("net.neoforged.neoforge.event.EventHooks;", "net.minecraftforge.event.ForgeEventFactory;"),
    ("net.neoforged.neoforge.common.CommonHooks;", "net.minecraftforge.common.ForgeHooks;"),
    ("net.neoforged.neoforge.client.ClientHooks;", "net.minecraftforge.client.ForgeHooksClient;"),
    ("net.neoforged.neoforge.event.tick.PlayerTickEvent;", "net.minecraftforge.event.TickEvent;"),
    ("net.neoforged.neoforge.client.event.ClientTickEvent;", "net.minecraftforge.event.TickEvent;"),
    ("net.neoforged.neoforge.registries.NeoForgeRegistries;", "net.minecraftforge.registries.ForgeRegistries;"),
    ("net.neoforged.neoforge.registries.DeferredHolder;", "net.minecraftforge.registries.RegistryObject;"),
    ("net.neoforged.neoforge.registries.DeferredItem;", "net.minecraftforge.registries.RegistryObject;"),
    ("net.neoforged.neoforge.common.conditions.ICondition;", "net.minecraftforge.common.crafting.conditions.ICondition;"),
    ("net.neoforged.neoforge.common.util.TriState;", "net.minecraftforge.eventbus.api.Event;"),
    ("net.neoforged.neoforge.", "net.minecraftforge."),
]
NAMES = [
    (r"\bNeoForge\.EVENT_BUS\b", "MinecraftForge.EVENT_BUS"),
    (r"\bEventHooks\.", "ForgeEventFactory."),
    (r"\bCommonHooks\.", "ForgeHooks."),
    (r"\bClientHooks\.", "ForgeHooksClient."),
    (r"\bNeoForgeRegistries\b", "ForgeRegistries"),
    (r"\bEventBusSubscriber\.Bus\.GAME\b", "EventBusSubscriber.Bus.FORGE"),
    (r"\bPlayerTickEvent\.(Pre|Post)\b", "TickEvent.PlayerTickEvent"),
    (r"\bClientTickEvent\.(Pre|Post)\b", "TickEvent.ClientTickEvent"),
    (r"\bResourceLocation\.fromNamespaceAndPath\(", "new ResourceLocation("),
    (r"\bResourceLocation\.withDefaultNamespace\(", "new ResourceLocation("),
    (r"\bResourceLocation\.parse\(", "new ResourceLocation("),
]


def convert(path: pathlib.Path) -> bool:
    text = path.read_text()
    out = text
    for old, new in IMPORTS:
        out = out.replace(old, new)
    for pat, new in NAMES:
        out = re.sub(pat, new, out)
    if out != text:
        path.write_text(out)
        return True
    return False


def main(args):
    files = []
    for a in args:
        p = pathlib.Path(a)
        files += sorted(p.rglob("*.java")) if p.is_dir() else [p]
    changed = sum(convert(f) for f in files)
    print(f"{changed} of {len(files)} files changed")


if __name__ == "__main__":
    main(sys.argv[1:])
