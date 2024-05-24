package net.frozenblock.trailiertales.block.entity.coffin;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public enum CoffinSpawnerState implements StringRepresentable {
	INACTIVE("inactive", 0, false),
	ACTIVE("active", 4, true),
	IRRITATED("irritated", 6, true),
	AGGRESSIVE("aggressive", 8, true);

	private final String name;
	private final int lightLevel;
	private final boolean isCapableOfSpawning;

	CoffinSpawnerState(final String name, int lightLevel, final boolean isCapableOfSpawning) {
		this.name = name;
		this.lightLevel = lightLevel;
		this.isCapableOfSpawning = isCapableOfSpawning;
	}

	CoffinSpawnerState tickAndGetNext(BlockPos pos, @NotNull CoffinSpawner spawner, ServerLevel level) {
		CoffinSpawnerData coffinSpawnerData = spawner.getData();
		CoffinSpawnerConfig coffinSpawnerConfig = spawner.getConfig();
		CoffinSpawnerState nextState;
		switch(this) {
			case INACTIVE:
				nextState = coffinSpawnerData.hasMobToSpawnAndSeesPlayer(spawner, level.random) ? ACTIVE : INACTIVE;
				break;
			case ACTIVE:
				if (!coffinSpawnerData.hasMobToSpawnAndSeesPlayer(spawner, level.random)) {
					nextState = INACTIVE;
				} else {
					int i = coffinSpawnerData.countAdditionalPlayers();
					coffinSpawnerData.tryDetectPlayers(level, pos, spawner);

					if (!coffinSpawnerData.isPowerCooldownFinished(level) && coffinSpawnerData.power >= coffinSpawnerConfig.powerForNextLevel()) {
						nextState = IRRITATED;
						coffinSpawnerData.powerCooldownEndsAt = level.getGameTime() + (long)spawner.getPowerCooldownLength();
						coffinSpawnerData.power = 0;
						break;
					}

					if (coffinSpawnerData.hasFinishedSpawningAllMobs(coffinSpawnerConfig, i)) {
						if (coffinSpawnerData.haveAllCurrentMobsDied()) {
							coffinSpawnerData.totalMobsSpawned = 0;
							coffinSpawnerData.nextMobSpawnsAt = 0L;
							coffinSpawnerData.power = 0;
						}
					} else if (coffinSpawnerData.isReadyToSpawnNextMob(level, coffinSpawnerConfig, i)) {
						spawner.spawnMob(level, pos).ifPresent(uuid -> {
							coffinSpawnerData.currentMobs.add(uuid);
							++coffinSpawnerData.totalMobsSpawned;
							coffinSpawnerData.nextMobSpawnsAt = level.getGameTime() + (long)coffinSpawnerConfig.ticksBetweenSpawn();
							coffinSpawnerConfig.spawnPotentials().getRandom(level.getRandom()).ifPresent(spawnData -> {
								coffinSpawnerData.nextSpawnData = Optional.of(spawnData.data());
								spawner.markUpdated();
							});
						});
					}

					nextState = this;
				}
				break;
			case IRRITATED:
				if (!coffinSpawnerData.hasMobToSpawnAndSeesPlayer(spawner, level.random)) {
					nextState = INACTIVE;
				} else {
					int i = coffinSpawnerData.countAdditionalPlayers();
					coffinSpawnerData.tryDetectPlayers(level, pos, spawner);

					if (!coffinSpawnerData.isPowerCooldownFinished(level) && coffinSpawnerData.power >= coffinSpawnerConfig.powerForNextLevel()) {
						nextState = AGGRESSIVE;
						coffinSpawnerData.powerCooldownEndsAt = level.getGameTime() + (long)spawner.getPowerCooldownLength();
						coffinSpawnerData.power = 0;
						break;
					}

					if (coffinSpawnerData.hasFinishedSpawningAllMobs(coffinSpawnerConfig, i)) {
						if (coffinSpawnerData.haveAllCurrentMobsDied()) {
							coffinSpawnerData.totalMobsSpawned = 0;
							coffinSpawnerData.nextMobSpawnsAt = 0L;
							coffinSpawnerData.power = 0;
						}
					} else if (coffinSpawnerData.isReadyToSpawnNextMob(level, coffinSpawnerConfig, i)) {
						spawner.spawnMob(level, pos).ifPresent(uuid -> {
							coffinSpawnerData.currentMobs.add(uuid);
							++coffinSpawnerData.totalMobsSpawned;
							coffinSpawnerData.nextMobSpawnsAt = level.getGameTime() + (long)coffinSpawnerConfig.ticksBetweenSpawn();
							coffinSpawnerConfig.spawnPotentials().getRandom(level.getRandom()).ifPresent(spawnData -> {
								coffinSpawnerData.nextSpawnData = Optional.of(spawnData.data());
								spawner.markUpdated();
							});
						});
					}

					nextState = this;
				}
				break;
			case AGGRESSIVE:
				if (!coffinSpawnerData.hasMobToSpawnAndSeesPlayer(spawner, level.random)) {
					nextState = INACTIVE;
				} else {
					int i = coffinSpawnerData.countAdditionalPlayers();
					coffinSpawnerData.tryDetectPlayers(level, pos, spawner);

					if (coffinSpawnerData.isPowerCooldownFinished(level)) {
						nextState = ACTIVE;
						coffinSpawnerData.totalMobsSpawned = 0;
						coffinSpawnerData.nextMobSpawnsAt = 0L;
						coffinSpawnerData.power = 0;
						break;
					}

					if (coffinSpawnerData.hasFinishedSpawningAllMobs(coffinSpawnerConfig, i)) {
						if (coffinSpawnerData.haveAllCurrentMobsDied()) {
							coffinSpawnerData.totalMobsSpawned = 0;
							coffinSpawnerData.nextMobSpawnsAt = 0L;
							coffinSpawnerData.power = 0;
						}
					} else if (coffinSpawnerData.isReadyToSpawnNextMob(level, coffinSpawnerConfig, i)) {
						spawner.spawnMob(level, pos).ifPresent(uuid -> {
							coffinSpawnerData.currentMobs.add(uuid);
							++coffinSpawnerData.totalMobsSpawned;
							coffinSpawnerData.nextMobSpawnsAt = level.getGameTime() + (long)coffinSpawnerConfig.ticksBetweenSpawn();
							coffinSpawnerConfig.spawnPotentials().getRandom(level.getRandom()).ifPresent(spawnData -> {
								coffinSpawnerData.nextSpawnData = Optional.of(spawnData.data());
								spawner.markUpdated();
							});
						});
					}

					nextState = this;
				}
				break;
			default:
				throw new MatchException(null, null);
		}

		return nextState;
	}

	public int getLightLevel() {
		return lightLevel;
	}

	public boolean isCapableOfSpawning() {
		return this.isCapableOfSpawning;
	}

	@Override
	public @NotNull String getSerializedName() {
		return this.name;
	}
}
