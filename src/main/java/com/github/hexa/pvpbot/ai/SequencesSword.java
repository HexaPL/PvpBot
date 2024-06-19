package com.github.hexa.pvpbot.ai;

import com.github.hexa.pvpbot.util.LogUtils;
import com.github.hexa.pvpbot.util.MathHelper;
import org.bukkit.Bukkit;

import static com.github.hexa.pvpbot.ai.ControllableBot.MoveDirection.*;
import static com.github.hexa.pvpbot.ai.SwordAi.HitType.CRITICAL_HIT;

public class SequencesSword extends SwordAi {

    private final SwordAi ai;
    private final ControllableBot bot;

    protected boolean hitWhileFalling = false;

    public SequencesSword(SwordAi ai) {
        this.ai = ai;
        this.bot = ai.bot;
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
                    this.nextStep();
                    break;
                case 3:
                    this.tickSubsequence(ai.sprintResetSequence);
                    break;
            }
        }
    };

    public final Sequence normalHit_counterCrits = new Sequence(3) {

        private boolean shouldDodgeJump;

        @Override
        public void onStart() {
            this.shouldDodgeJump = false;
            ai.setMoveForward(FORWARD);
        }

        @Override
        public void onTick() {
            switch (step) {
                case 1:
                    if (ai.target.ticksSinceJump == 0) {
                        double jumpDistance = ai.getPingDistance();
                        //Bukkit.broadcastMessage("JUMP distance: " + MathHelper.roundTo(jumpDistance, 3) + ", " + LogUtils.getTimeString());
                        if (jumpDistance > 6.4 && jumpDistance < 7 && ai.target.blockSpeedTowardsBot > 0) {
                            this.shouldDodgeJump = true;
                            //Bukkit.broadcastMessage("Dodging jump");
                        }
                    }
                    if (this.shouldDodgeJump) {
                        this.tickSubsequence(dodgeJump);
                        if (this.subSequence == null) {
                            this.shouldDodgeJump = false;
                        } else {
                            break;
                        }
                    }
                    this.waitUntil(ai::canAttack);
                    break;
                case 2:
                    hitWhileFalling = !ai.target.getPlayer().isOnGround();
                    ai.doAttack();
                    this.nextStep();
                    break;
                case 3:
                    if (hitWhileFalling) {
                        this.tickSubsequence(sTap);
                    } else {
                        this.tickSubsequence(ai.sprintResetSequence);
                    }
                    break;
            }
        }

    };

    private final Sequence dodgeJump = new Sequence(3) {
        @Override
        public void onTick() {
            switch (step) {
                case 1:
                    ai.setMoveForward(BACKWARD);
                    break;
                case 2:
                    this.wait(1);
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

    public final Sequence reachCut = new Sequence(3) {
        @Override
        public void onStart() {
            ai.setMoveForward(FORWARD);
        }

        @Override
        public void onTick() {
            switch (step) {
                case 1:
                    this.waitUntil(ai::canAttack);
                    if (ai.getPingDistance() - 3.0F < (2 * ai.blockSpeed + 0.2F) && ai.getPingDistance() - 3.0F > (2 * ai.blockSpeed) && bot.isOnGround()) {
                        //Bukkit.broadcastMessage("reachCut: " + MathHelper.roundTo(ai.getPingDistance(), 3) + ", " + LogUtils.getTimeString());
                        bot.jump();
                    }
                    break;
                case 2:
                    ai.doAttack();
                    this.nextStep();
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
                    this.nextStep();
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
                    this.waitUntil(ai::canAttack);
                    break;
                case 6:
                    ai.doAttack();
                    this.nextStep();
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

    public final Sequence critDeflection = new Sequence(4) {
        @Override
        public void onStart() {
            ai.setMoveForward(FORWARD);
        }

        @Override
        public void onTick() {
            switch (step) {
                case 1:
                    if (this.waitUntil(() -> ai.target.ticksSinceJump == 0)) Bukkit.broadcastMessage("JUMP distance: " + MathHelper.roundTo(ai.getPingDistance(), 3) + ", " + LogUtils.getTimeString());
                    break;
                case 2:
                    this.waitUntil(() -> ai.target.getPlayer().isOnGround() && ai.canAttack());
                    break;
                case 3:
                    ai.doAttack();
                    this.nextStep();
                    break;
                case 4:
                    this.tickSubsequence(ai.sprintResetSequence);
                    break;

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
                    if (this.subSequence == null) this.nextStep();
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
                    if (this.subSequence == null) this.nextStep();
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
                this.tickStep(9); // To end sprint reset safely
            }
        }
    };

    public final Sequence wTap = new Sequence(4) {

        @Override
        public void onTick() {
            switch (step) {
                case 1:
                    this.wait(ai.getSprintResetDelay());
                    break;
                case 2:
                    ai.setMoveForward(0);
                    break;
                case 3:
                    if (ai.ticksSinceDamage == 2) {
                        this.stop();
                        break;
                    }
                    this.waitUntil(() -> ai.getPingDistance() > 3.5, 8);
                    break;
                case 4:
                    ai.setMoveForward(FORWARD);
                    break;
            }
        }

        @Override
        public void onStop() {
            ai.setMoveForward(FORWARD);
        }
    };



    public final Sequence wTap_counterRunning = new Sequence(4) {

        private int tick = 0;
        private boolean running = false;

        @Override
        public void onTick() {
            switch (step) {
                case 1:
                    //Bukkit.broadcastMessage("Start sprint reset" + ", Y: " + MathHelper.roundTo((float) ai.target.motion.getY(), 3) + ", motTar: " + MathHelper.roundTo(ai.getTarget().blockSpeedTowardsBot, 3) + ", lY: " + MathHelper.roundTo(ai.target.getLocation().getY(), 3));
                    tick = 1;
                    running = false;
                    ai.setMoveForward(0);
                    break;
                case 2:
                    //Bukkit.broadcastMessage("Tick " + tick + ", D: " + MathHelper.roundTo((float) ai.getPingDistance(), 3) + ", motY: " + MathHelper.roundTo((float) ai.target.motion.getY(), 3) + ", motTar: " + MathHelper.roundTo(ai.getTarget().blockSpeedTowardsBot, 3) + ", lY: " + MathHelper.roundTo(ai.target.getLocation().getY(), 3));
                    if (ai.ticksSinceDamage == 2) {
                        //Bukkit.broadcastMessage("Hit while sprint resetting!");
                        this.stop();
                        break;
                    }
                    if (ai.isTargetComboRunning()) {
                        running = true;
                        ai.setMoveForward(FORWARD);
                        this.stopTimer();
                        break;
                    }
                    this.waitUntil(() -> ai.getPingDistance() > 3.5, 8);
                    tick++;
                    break;
                case 3:
                    //Bukkit.broadcastMessage("Stop sprint reset" + ", tMot: " + MathHelper.roundTo(ai.getTarget().blockSpeedTowardsBot, 3));
                    ai.setMoveForward(FORWARD);
                    break;
                case 4:
                    if (running) {
                        ai.bot.jump();
                    }
                    break;
            }
        }

        @Override
        public void onStop() {
            ai.setMoveForward(FORWARD);
        }
    };

    public final Sequence sTap = new Sequence(6) {

        @Override
        public void onTick() {
            switch (step) {
                case 1:
                    this.wait(ai.getSprintResetDelay());
                    break;
                case 2:
                    ai.setMoveForward(0);
                    break;
                case 3:
                    ai.setMoveForward(BACKWARD);
                    break;
                case 4:
                    int len = hitWhileFalling ? 4 : ai.getSTapLength();
                    this.wait(len);
                    break;
                case 5:
                    ai.setMoveForward(0);
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

    public final Sequence upperCut = new Sequence(5) {
        @Override
        public void onStart() {
            ai.setMoveForward(FORWARD);
        }

        @Override
        public void onTick() {
            switch (step) {
                case 1:
                    this.waitUntil(() -> bot.isOnGround() && ai.getPingDistance() - 2 * ai.blockSpeed <= 3.0F); // TODO - use opponent motion too
                    break;
                case 2:
                    bot.jump();
                    break;
                case 3:
                    this.waitUntil(ai::canAttack);
                    break;
                case 4:
                    ai.doAttack();
                    this.nextStep();
                    break;
                case 5:
                    this.tickSubsequence(sTap);
                    break;
            }
        }
    };

    // TODO - more calculations...
    public final Sequence upperCut_counterRunning = new Sequence(7) {
        private boolean running;

        @Override
        public void onStart() {
            running = false;
            ai.setMoveForward(FORWARD);
        }

        @Override
        public void onTick() {
            switch (step) {
                case 1:
                    this.waitUntil(() -> bot.isOnGround() && ai.getPingDistance() - 2 * ai.blockSpeed <= 3.0F); // TODO - use opponent motion too
                    break;
                case 2:
                    bot.jump();
                    break;
                case 3:
                    this.waitUntil(ai::canAttack);
                    break;
                case 4:
                    ai.doAttack();
                    this.nextStep();
                    break;
                case 5:
                    Bukkit.broadcastMessage("Tick, D: " + MathHelper.roundTo((float) ai.getPingDistance(), 3) + ", motY: " + MathHelper.roundTo((float) ai.target.motion.getY(), 3) + ", motTar: " + MathHelper.roundTo(ai.getTarget().blockSpeedTowardsBot, 3) + ", lY: " + MathHelper.roundTo(ai.target.getLocation().getY(), 3));
                    if (ai.isTargetComboRunning()) {
                        running = true;
                        this.stopSubsequence();
                        break;
                    }
                    this.tickSubsequence(sTap);
                    if (this.subSequence == null && !running) {
                        this.stop();
                    }
                    break;
                case 6:
                    this.waitUntil(bot::canJump);
                    break;
                case 7:
                    bot.jump();
                    break;
            }
        }
    };

    public final Sequence hop = new Sequence(6) {
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
                    break;
                case 4:
                    bot.jump();
                    break;
                case 5:
                    this.wait(0);
                    break;
                case 6:
                    ai.setMoveForward(FORWARD);
                    break;
            }
        }
    };

    /*
    public final Sequence hop_counterRunning = new Sequence(8) {
        private boolean running = false;

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
                    if (ai.isTargetComboRunning()) {
                        running = true;
                        this.stopTimer();
                        break;
                    }
                    this.wait(3);
                    break;
                case 4:
                    if (running) {
                        this.nextStep();
                    } else {
                        ai.setMoveForward(0);
                    }
                    break;
                case 5:
                    this.wait(0);
                    break;
                case 6:
                    bot.jump();
                    break;
                case 7:
                    this.wait(0);
                    break;
                case 8:
                    ai.setMoveForward(FORWARD);
                    break;
            }
        }
    };*/

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
