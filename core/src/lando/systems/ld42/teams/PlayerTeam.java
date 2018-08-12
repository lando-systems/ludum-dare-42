package lando.systems.ld42.teams;

import lando.systems.ld42.Assets;
import lando.systems.ld42.Config;
import lando.systems.ld42.units.*;
import lando.systems.ld42.world.Castle;
import lando.systems.ld42.world.Tile;
import lando.systems.ld42.world.World;

public class PlayerTeam extends Team {

    public PlayerTeam(World world, Assets assets) {
        super(world);

        this.color = Config.player_color;

        Unit peasant = new PeasantUnit(assets);
        Unit soldier = new SoldierUnit(assets);
        Unit archer  = new ArcherUnit(assets);
        Unit wizard  = new WizardUnit(assets);

        Tile peasantTile = world.getTile(0, World.WORLD_HEIGHT / 2 - 1);
        Tile soldierTile = world.getTile(1, World.WORLD_HEIGHT / 2 - 1);
        Tile archerTile = world.getTile(0, World.WORLD_HEIGHT / 2 - 2);
        Tile wizardTile  = world.getTile(1, World.WORLD_HEIGHT / 2 - 2);

        peasant.moveTo(peasantTile);
        soldier.moveTo(soldierTile);
        archer.moveTo(archerTile);
        wizard.moveTo(wizardTile);

        this.units.add(peasant, soldier, archer, wizard);
        for (Unit unit : units) {
            unit.shadowColor.set(this.color.cpy());
            unit.shadowColor.a = 0.75f;
            unit.team = Type.player;
        }

        this.castle = new Castle(assets, Type.player, world.getTile(0, World.WORLD_HEIGHT / 2));
    }

}
