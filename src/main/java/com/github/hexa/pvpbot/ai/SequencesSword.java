package com.github.hexa.pvpbot.ai;

import com.github.hexa.pvpbot.util.MathHelper;

import static com.github.hexa.pvpbot.ai.ControllableBot.MoveDirection.*;
import static com.github.hexa.pvpbot.ai.SwordAi.HitType.CRITICAL_HIT;
import static com.github.hexa.pvpbot.ai.SwordAi.SprintResetMethod.S_TAP;

public class SequencesSword extends SwordAi {

    private final SwordAi ai;

    public SequencesSword(SwordAi ai) {
        this.ai = ai;
    }

    public final Sequence normalHit = new Sequence(3) {
        @Override
        public void onStart() {
            ai.setMoveForward(FORWARD);
        }

        @Override
        public void onTick() {
            switch (step) {
                case 1:
                    this.waitUntil(ai::canAttack);
                    break;
                case 2:
                    ai.doAttack();
                    break;
                case 3:
                    this.tickSubsequence(ai.sprintResetSequence);
                    break;
            }
        }
    };

    public final Sequence hitSelect = new Sequence(5) {
        @Override
        public void onStart() {
            ai.setMoveForward(FORWARD);
        }

        @Override
        public void onTick() {
            switch (step) {
                case 1:
                    this.waitUntil(() -> ai.ticksSinceDamage == 0 || ai.getPingDistance() < 2.5F);
                    break;
                case 2:
                    this.wait(5);
                    break;
                case 3:
                    this.waitUntil(ai::canAttack);
                    break;
                case 4:
                    ai.doAttack();
                    break;
                case 5:
                    this.tickSubsequence(ai.sprintResetSequence);
                    break;
            }
        }
    };

    public final Sequence bait = new Sequence(7) {
        @Override
        public void onStart() {
            ai.setMoveForward(FORWARD);
        }

        @Override
        public void onTick() {
            switch (step) {
                case 1:
                    this.waitUntil(() -> ai.getPingDistance() < 5.5); // TODO - actual calculations
                    break;
                case 2:
                    ai.setMoveForward(BACKWARD);
                    break;
                case 3:
                    this.wait(5);
                    break;
                case 4:
                    ai.setMoveForward(FORWARD);
                    break;
                case 5:
                    this.waitUntil(ai::canHit);
                    break;
                case 6:
                    ai.doAttack();
                    break;
                case 7:
                    this.tickSubsequence(ai.sprintResetSequence);
                    break;
            }
        }

        @Override
        public void onStop() {
            if (ai.bot.getMoveForward() != FORWARD) {
                ai.setMoveForward(FORWARD);
            }
        }
    };

    public final Sequence jumpAndCrit = new Sequence(6) {
        @Override
        public void onStart() {
            ai.setMoveForward(FORWARD);
        }

        @Override
        public void onTick() {
            switch (step) {
                case 1:
                    this.tickSubsequence(ai.sequences.jump);
                    break;
                case 2:
                    this.wait(7); // Time for optimal crit
                    break;
                case 3:
                    if (ai.bot.isOnGround()) { // Stop the sequence if bot landed before it could hit the player
                        this.stop();
                        break;
                    }
                    this.nextStep();
                    break;
                case 4:
                    this.tickSubsequence(crit);
                    break;
                case 5:
                    this.wait(2);
                    break;
                case 6:
                    ai.setMoveForward(FORWARD);
                    break;
            }
        }

        @Override
        public void onStop() {
            ai.setMoveForward(FORWARD);
        }
    };

    public final Sequence crit = new Sequence(3) {
        @Override
        public void onTick() {
            switch (step) {
                case 1:
                    ai.setMoveForward(0);
                    break;
                case 2:
                    this.wait(2);
                    break;
                case 3:
                    if (ai.canHit()) {
                        ai.doAttack(CRITICAL_HIT);
                    }
                    break;
            }
        }
    };

    public final Sequence wasdHit = new Sequence(9) {
        @Override
        public void onTick() {
            switch (step) {
                case 1:
                    this.waitUntil(ai::canAttack);
                    break;
                case 2:
                    ai.doAttack();
                    break;
                case 3:
                    ai.setMoveForward(0);
                    ai.setMoveStrafe(LEFT);
                    ai.bot.setSprinting(false);
                    break;
                case 4:
                    this.wait(1);
                    break;
                case 5:
                    ai.setMoveForward(BACKWARD);
                    ai.setMoveStrafe(0);
                    break;
                case 6:
                    this.wait(1);
                    break;
                case 7:
                    ai.setMoveForward(0);
                    ai.setMoveStrafe(RIGHT);
                    break;
                case 8:
                    this.wait(1);
                    break;
                case 9:
                    ai.setMoveForward(FORWARD);
                    ai.setMoveStrafe(0);
                    break;
            }
        }

        @Override
        public void onStop() {
            if (!finished) {
                this.tickStep(8); // To end sprint reset safely
            }
        }
    };

    public final Sequence wTap = new Sequence(3) {

        @Override
        public void onTick() {
            switch (step) {
                case 1:
                    ai.setMoveForward(0);
                    break;
                case 2:
                    this.wait(ai.getWTapLength());
                    break;
                case 3:
                    ai.setMoveForward(FORWARD);
                    break;
            }
        }

        @Override
        public void onStop() {
            ai.setMoveForward(FORWARD);
        }
    };

    public final Sequence sTap = new Sequence(3) {

        @Override
        public void onTick() {
            switch (step) {
                case 1:
                    ai.setMoveForward(BACKWARD);
                    break;
                case 2:
                    this.wait(ai.getSTapLength());
                    break;
                case 3:
                    ai.setMoveForward(FORWARD);
                    break;
            }
        }

        @Override
        public void onStop() {
            ai.setMoveForward(FORWARD);
        }
    };

    public final Sequence upperCut = new Sequence(3) {
        @Override
        public void onStart() {
            ai.setMoveForward(FORWARD);
        }

        @Override
        public void onTick() {
            switch (step) {
                case 1:
                    this.waitUntil(ai::canAttack);
                    break;
                case 2:
                    ai.bot.jump();
                    ai.doAttack();
                    break;
                case 3:
                    this.tickSubsequence(ai.sprintResetSequence);
                    break;
            }
        }
    };

    public final Sequence straightlineCombo = Sequence.empty();

    public final Sequence noStrafe = new Sequence(1) {
        @Override
        public void onTick() {
            ai.setMoveStrafe(0);
        }
    };

    public final Sequence switchStrafe = new Sequence(3) {
        private int previousDirection = RIGHT;

        @Override
        public void onTick() {
            switch (step) {
                case 1:
                    int newDirection = previousDirection == RIGHT ? LEFT : RIGHT;
                    ai.setMoveStrafe(newDirection);
                    previousDirection = newDirection;
                    break;
                case 2:
                    this.waitUntil(() -> ai.ticksSinceAttack >= 15 || ai.ticksSinceDamage == 1);
                    break;
                case 3:
                    ai.setMoveStrafe(0);
                    break;
            }
        }
    };

    public final Sequence circleStrafe = new Sequence(3) {
        private int direction = MathHelper.random(1, 2);
        private boolean changeDirection = false;

        @Override
        public void onStart() {
            if (this.changeDirection) {
                this.changeDirection = false;
                this.direction = MathHelper.random(1, 2);
            }
        }

        @Override
        public void onTick() {
            switch (step) {
                case 1:
                    if (MathHelper.chanceOf(0.15F)) {
                        this.direction = (direction == 1 ? 2 : 1);
                    }
                    ai.setMoveStrafe(direction == 1 ? LEFT : RIGHT);
                    break;
                case 2:
                    this.waitUntil(() -> ai.ticksSinceAttack >= 15 || ai.ticksSinceDamage == 1);
                    break;
                case 3:
                    ai.setMoveStrafe(0);
                    this.changeDirection = true;
                    break;
            }
        }
    };

    // TODO - working sprintcut
    public final Sequence sprintcutCombo = new Sequence(2) {
        @Override
        public void onTick() {
            switch (step) {
                case 1:
                    this.waitUntil(() -> wTap.finished);
                    break;
                case 2:
                    ai.bot.jump();
                    break;
            }
        }
    };

    public final Sequence critSpam = Sequence.empty();

    public final Sequence jump = new Sequence(2) {
        @Override
        public void onTick() {
            switch (step) {
                case 1:
                    this.waitUntil(() -> ai.bot.canJump());
                    break;
                case 2:
                    ai.bot.jump();
                    break;
            }
        }
    };

    /* TODO - pre-firsthit strafe

    public final Sequence counterStrafe = new Sequence(2) {
        @Override
        public void onTick() {
            switch (step) {
                case 1:
                    this.waitUntil(() -> ai.getPingDistance() < SwordAi.maxStrafeDistance && ai.firstHit);
                    break;
                case 2:
                    if (ai.getPingDistance() < SwordAi.maxStrafeDistance && ai.firstHit) {
                        ai.bot.setMoveStrafe(ai.target.strafeDirection);
                        this.keepStep();
                    } else {
                        this.stop();
                    }
                    break;
            }
        }

        @Override
        public void onStop() {
            ai.bot.setMoveStrafe(0);
        }
    };

    public final Sequence switchStrafe = new Sequence(2) {
        private float lastDirection = 1;

        @Override
        public void onTick() {
            switch (step) {
                case 1:
                    this.waitUntil(() -> ai.getPingDistance() < SwordAi.maxStrafeDistance && ai.firstHit);
                    break;
                case 2:
                    if (ai.getPingDistance() < SwordAi.maxStrafeDistance && ai.firstHit) {
                        ai.bot.setMoveStrafe(lastDirection * -1);
                        this.keepStep();
                    } else {
                        this.stop();
                    }
                    break;
            }
        }

        @Override
        public void onStop() {
            lastDirection = ai.bot.getMoveStrafe();
            ai.bot.setMoveStrafe(0);
        }
    };*/

}
