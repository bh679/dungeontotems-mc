"""Regex renames for 26.3 over a source tree. usage: renames.py <root>"""
import os, re, sys
R = [
 (r'\.serverLevel\(\)', '.level()'),
 (r'\bMobEffects\.MOVEMENT_SLOWDOWN\b', 'MobEffects.SLOWNESS'),
 (r'\bMobEffects\.DIG_SLOWDOWN\b', 'MobEffects.MINING_FATIGUE'),
 (r'\bMobEffects\.MOVEMENT_SPEED\b', 'MobEffects.SPEED'),
 (r'\bMobEffects\.DIG_SPEED\b', 'MobEffects.HASTE'),
 (r'\bMobEffects\.DAMAGE_BOOST\b', 'MobEffects.STRENGTH'),
 (r'\bMobEffects\.HEAL\b', 'MobEffects.INSTANT_HEALTH'),
 (r'\bMobEffects\.HARM\b', 'MobEffects.INSTANT_DAMAGE'),
 (r'\bMobEffects\.JUMP\b', 'MobEffects.JUMP_BOOST'),
 (r'\bMobEffects\.CONFUSION\b', 'MobEffects.NAUSEA'),
 (r'\bMobEffects\.DAMAGE_RESISTANCE\b', 'MobEffects.RESISTANCE'),
 (r'\.moveTo\(', '.snapTo('),
 (r'\.getMinBuildHeight\(\)', '.getMinY()'),
 (r'\.getMaxBuildHeight\(\)', '.getMaxY()'),
 (r'\.displayClientMessage\(([^;]*?),\s*true\)', r'.sendOverlayMessage(\1)'),
 (r'\.displayClientMessage\(([^;]*?),\s*false\)', r'.sendSystemMessage(\1)'),
 (r'\.isInWaterRainOrBubble\(\)', '.isInWaterOrRain()'),
 (r'\.isInWaterOrBubble\(\)', '.isInWater()'),
 (r'\.registryOrThrow\(', '.lookupOrThrow('),
 (r'InteractionResult\.sidedSuccess\([^)]*\)', 'InteractionResult.SUCCESS'),
 (r'\bmc\.screen\b', 'mc.gui.screen()'),
 (r'\bminecraft\.screen\b', 'minecraft.gui.screen()'),
 (r'Minecraft\.getInstance\(\)\.screen\b', 'Minecraft.getInstance().gui.screen()'),
 (r'\bmc\.setScreen\(', 'mc.gui.setScreen('),
 (r'\bminecraft\.setScreen\(', 'minecraft.gui.setScreen('),
 (r'Minecraft\.getInstance\(\)\.setScreen\(', 'Minecraft.getInstance().gui.setScreen('),
 (r'\.getHolderOrThrow\(', '.getOrThrow('),
 (r'\bplayer\.server\b', 'player.level().getServer()'),
 (r'\.getFoodProperties\(\w+\) != null', '.has(DataComponents.FOOD)'),
 (r'FMLEnvironment\.dist\b', 'FMLEnvironment.getDist()'),
 (r'(\w+)\.getDayTime\(\)', r'\1.getOverworldClockTime()'),
 (r'PacketDistributor\.sendToServer\(', 'ClientPacketDistributor.sendToServer('),
]
root = sys.argv[1]
for dp, _, fs in os.walk(root):
    for f in fs:
        if not f.endswith('.java'): continue
        p = os.path.join(dp, f); t = open(p, encoding='utf-8').read(); o = t
        for a, b in R: t = re.sub(a, b, t)
        if 'ClientPacketDistributor.sendToServer(' in t and 'import net.neoforged.neoforge.client.network.ClientPacketDistributor;' not in t:
            t = re.sub(r'(\nimport )', r'\nimport net.neoforged.neoforge.client.network.ClientPacketDistributor;\1', t, 1)
        if t != o: open(p, 'w', encoding='utf-8').write(t); print('renamed', os.path.relpath(p, root))
