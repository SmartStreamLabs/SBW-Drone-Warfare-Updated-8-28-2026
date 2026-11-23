package nl.smartstreamlabs.sbwdroneconfig;
import org.junit.jupiter.api.Test;import static org.junit.jupiter.api.Assertions.*;
class Qd01IkTest {
 @Test void bindAndNormalGroundContactsStayConnected(){
  for(boolean left:new boolean[]{false,true})for(double lift:new double[]{0,.03,.08,.12})for(double step:new double[]{-.18,-.09,0,.09,.18})for(double sideways:new double[]{-.03,0,.03}){
   double x=(left?-.15:.15)+sideways,y=-.745+lift,z=-.045+step;var pose=Qd01Ik.solve(left,x,y,z,0);var actual=Qd01Ik.contact(left,pose);
   assertFalse(pose.limited());assertEquals(x,actual[0],.006);assertEquals(y-.075,actual[1],.006);assertEquals(z,actual[2],.006);
  }
  var bind=Qd01Ik.solve(true,-.15,-.745,-.045,0);assertEquals(0,bind.thigh(),1e-6);assertEquals(0,bind.shin(),1e-6);
 }
 @Test void unreachableGroundIsBounded(){var p=Qd01Ik.solve(true,-5,8,20,0);assertTrue(p.limited());assertTrue(Double.isFinite(p.hip()+p.thigh()+p.shin()+p.foot()));assertTrue(Math.abs(p.hip())<=.32);}
}
