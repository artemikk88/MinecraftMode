# Repair Forge — мод для Prominence 2: Hasturian Era (Fabric 1.20.1)

Добавляет один блок — **Ремонтный горн**. Работает как печка:
кладёшь повреждённый предмет + любое печное топливо → прочность восстанавливается
(10 единиц в секунду, настраивается в `RepairForgeBlockEntity.TICKS_PER_DURABILITY`).
Полностью починенный предмет перекладывается в выходной слот.

## Сборка

1. Установите JDK 17.
2. Скачайте шаблон https://github.com/FabricMC/fabric-example-mod (ветка 1.20) или
   просто создайте эти файлы в пустой папке вместе с `gradlew` из шаблона.
3. `./gradlew build` → jar в `build/libs/repairforge-1.0.0.jar`.
4. Положите jar в папку `mods` модпака.

## Текстуры

Сейчас используются ванильные текстуры печки как заглушка. Чтобы поставить свои:
- `assets/repairforge/textures/block/repair_forge_top.png`, `_side.png`, `_front.png`, `_front_on.png`
  и пропишите их в `models/block/repair_forge*.json`.
- `assets/repairforge/textures/gui/repair_forge.png` (176×166, индикаторы огня/стрелки по
  координатам furnace.png) и поменяйте `TEXTURE` в `RepairForgeScreen`.
- `assets/repairforge/icon.png` — иконка мода.

## Баланс

- Тег `repairforge:repair_blacklist` — предметы, которые чинить нельзя.
- Крафт — `data/repairforge/recipes/repair_forge.json`.
