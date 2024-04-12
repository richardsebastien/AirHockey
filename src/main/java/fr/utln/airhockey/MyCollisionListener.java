package fr.utln.airhockey;

import com.jme3.bullet.collision.PhysicsCollisionEvent;
import com.jme3.bullet.collision.PhysicsCollisionListener;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;

public class MyCollisionListener implements PhysicsCollisionListener {

    private RigidBodyControl raquette;
    private RigidBodyControl mur1;
    private RigidBodyControl mur2;
    private RigidBodyControl mur3;
    private RigidBodyControl mur4;

    public MyCollisionListener(RigidBodyControl raquette,RigidBodyControl mur1, RigidBodyControl mur2,RigidBodyControl mur3, RigidBodyControl mur4) {
        this.raquette = raquette;
        this.mur1 = mur1;
        this.mur2 = mur2;
        this.mur3 = mur3;
        this.mur4 = mur4;
    }

    @Override
    public void collision(PhysicsCollisionEvent event) {
        Vector3f pommee;
        if ((event.getObjectA() == raquette || event.getObjectB() == raquette) && (event.getObjectA() == mur1 || event.getObjectB() == mur1)) {
            System.out.println("Collision mur1");
            pommee = raquette.getLinearVelocity();
            raquette.setLinearVelocity(new Vector3f(pommee.getX(), 0, pommee.getZ()));
        }
        if ((event.getObjectA() == raquette || event.getObjectB() == raquette) && (event.getObjectA() == mur2 || event.getObjectB() == mur2)) {
            System.out.println("Collision mur2");
            pommee = raquette.getLinearVelocity();
            raquette.setLinearVelocity(new Vector3f(pommee.getX(), 0, pommee.getZ()));
        }
        if ((event.getObjectA() == raquette || event.getObjectB() == raquette) && (event.getObjectA() == mur3 || event.getObjectB() == mur3)) {
            System.out.println("Collision mur3");
            pommee = raquette.getLinearVelocity();
            raquette.setLinearVelocity(new Vector3f(pommee.getX(), 0, pommee.getZ()));
        }
        if ((event.getObjectA() == raquette || event.getObjectB() == raquette) && (event.getObjectA() == mur4 || event.getObjectB() == mur4)){
            System.out.println("Collision mur4");
            pommee = raquette.getLinearVelocity();
            raquette.setLinearVelocity(new Vector3f(pommee.getX(), 0, pommee.getZ()));
        }

    }

}