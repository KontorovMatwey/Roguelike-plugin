package game;

public enum ItemType {

    REGEN("HP regen", 3),
    FAST_ATTACK("x0.8 attack cooldown", 3),
    POWER_ATTACK("+1 damage, x1.2 attack cooldown", 3),
    SMALL_FAST("x0.8 size, x1.25 speed", 3),
    DIAGONAL_SHOT("Diagonal shots, x0.8 projectile speed", 1);

    private final String label;
    private final int maxCopies;

    ItemType(String label, int maxCopies) {
        this.label = label;
        this.maxCopies = maxCopies;
    }

    public String getLabel() {
        return label;
    }

    public int getMaxCopies() {
        return maxCopies;
    }

    public boolean canDropAgain(int ownedCount) {
        return ownedCount < maxCopies;
    }

    public void applyTo(Player player) {
        switch (this) {
            case REGEN -> player.addHpRegenStack();
            case FAST_ATTACK -> player.multiplyAttackCooldown(0.8);
            case POWER_ATTACK -> {
                player.addDamage(1);
                player.multiplyAttackCooldown(1.2);
            }
            case SMALL_FAST -> {
                player.scaleSize(0.8);
                player.multiplySpeed(1.25);
            }
            case DIAGONAL_SHOT -> player.enableDiagonalShots();
        }
    }
}