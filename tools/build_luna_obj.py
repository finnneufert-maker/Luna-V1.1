"""Authoring source for Luna's editable OBJ models. Run with Python 3 (stdlib only)."""
from math import cos, sin, pi, sqrt
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1] / 'app/src/main/assets'

class Obj:
    def __init__(self):
        self.lines=['# Luna Yamanaka: sculptable mesh asset. Coordinates: Y up, Z front.']
        self.count=0
        self.normal_count=0
    def mesh(self,name,mat,vertices,faces):
        self.lines += [f'o {name}',f'usemtl {mat}']
        normals=[[0.,0.,0.] for _ in vertices]
        for a,b,c in faces:
            p,q,r=(vertices[i] for i in (a,b,c))
            u=[q[i]-p[i] for i in range(3)];v=[r[i]-p[i] for i in range(3)]
            n=(u[1]*v[2]-u[2]*v[1],u[2]*v[0]-u[0]*v[2],u[0]*v[1]-u[1]*v[0]);length=sqrt(sum(x*x for x in n)) or 1
            for corner in (a,b,c):
                for axis in range(3):normals[corner][axis]+=n[axis]/length
        normals=[tuple(x/(sqrt(sum(y*y for y in n)) or 1) for x in n) for n in normals]
        for p in vertices:self.lines.append('v %.5f %.5f %.5f'%tuple(p))
        for n in normals:self.lines.append('vn %.5f %.5f %.5f'%n)
        for a,b,c in faces:
            self.lines.append('f '+' '.join(f'{self.count+i+1}//{self.normal_count+i+1}' for i in (a,b,c)))
        self.count += len(vertices)
        self.normal_count += len(normals)
    def save(self,path):
        path.write_text('\n'.join(self.lines)+'\n')

def surface(obj,name,mat,rows,sides,fn,closed=False):
    verts=[fn(i/(rows-1),j*2*pi/sides) for i in range(rows) for j in range(sides)]
    faces=[]
    for i in range(rows-1):
        for j in range(sides):
            a=i*sides+j;b=i*sides+(j+1)%sides;c=(i+1)*sides+j;d=(i+1)*sides+(j+1)%sides
            faces.extend(((a,b,c),(b,d,c)))
    if closed:
        verts.extend((fn(0,0),fn(1,0)))
        for j in range(sides):
            faces.extend(((len(verts)-2,j,(j+1)%sides),((rows-1)*sides+j,len(verts)-1,(rows-1)*sides+(j+1)%sides)))
    obj.mesh(name,mat,verts,faces)

def ellipsoid(obj,name,mat,x,y,z,rx,ry,rz):
    surface(obj,name,mat,24,32,lambda t,a:(x+rx*sin(pi*t)*cos(a),y+ry*cos(pi*t),z+rz*sin(pi*t)*sin(a)))

def loft(obj,name,mat,rings,steps=36):
    # Hand-defined radial cross sections: (Y, width, depth, Z center).
    def fn(t,a):
        at=t*(len(rings)-1);i=min(len(rings)-2,int(at));u=at-i
        smooth=u*u*(3-2*u)
        values=[rings[i][k]*(1-smooth)+rings[i+1][k]*smooth for k in range(4)]
        y,w,d,z=values
        return (w*cos(a),y,z+d*sin(a))
    surface(obj,name,mat,(len(rings)-1)*5+1,steps,fn,True)

def tube(obj,name,mat,points,radii,sides=16):
    # Curved strands, limbs, and tail are continuous tapered tubes.
    vertices=[];faces=[]
    for i,(x,y,z) in enumerate(points):
        prev=points[max(i-1,0)];nxt=points[min(i+1,len(points)-1)]
        tx=nxt[0]-prev[0];ty=nxt[1]-prev[1];tz=nxt[2]-prev[2]
        ln=sqrt(tx*tx+ty*ty+tz*tz) or 1;tx/=ln;ty/=ln;tz/=ln
        ax,ay,az=ty,-tx,0;ln=sqrt(ax*ax+ay*ay) or 1;ax/=ln;ay/=ln
        bx=ty*az-tz*ay;by=tz*ax-tx*az;bz=tx*ay-ty*ax
        for j in range(sides):
            a=2*pi*j/sides;r=radii[i]
            vertices.append((x+r*(ax*cos(a)+bx*sin(a)),y+r*(ay*cos(a)+by*sin(a)),z+r*(az*cos(a)+bz*sin(a))))
    for i in range(len(points)-1):
        for j in range(sides):
            a=i*sides+j;b=i*sides+(j+1)%sides;c=(i+1)*sides+j;d=(i+1)*sides+(j+1)%sides
            faces.extend(((a,c,b),(b,c,d)))
    obj.mesh(name,mat,vertices,faces)

def cat_ear(obj,side,scale):
    x=side*.28*scale;base=1.12*scale;z=.01*scale
    def shape(t,a):
        width=(.20*(1-t)**.72+.012)*scale;depth=(.115*(1-t)+.015)*scale
        return (x+side*.12*t*scale+width*cos(a),base+.42*t*scale,z+.05*t*scale+depth*sin(a))
    surface(obj,'ear_'+str(side),'hair',12,32,shape)
    # Pink inner ear follows the forward face, with a rounded top.
    def inner(t,a):
        width=(.13*(1-t)**.7+.005)*scale
        return (x+side*.12*t*scale+width*cos(a),base+(.035+.31*t)*scale,(.13+.03*t)*scale+.006*scale*sin(a))
    surface(obj,'ear_inner_'+str(side),'pink',10,20,inner)

def build(chibi=False):
    o=Obj();s=1 if not chibi else .94
    # A narrowed jaw and continuous back of head, with silver hair behind it.
    ellipsoid(o,'hair_shell','hair',0,.69*s,-.075*s,.39*s,.49*s,.30*s)
    loft(o,'face','skin',[(1.10*s,.23*s,.18*s,.18*s),(.99*s,.33*s,.25*s,.19*s),(.75*s,.38*s,.28*s,.20*s),(.48*s,.34*s,.25*s,.19*s),(.27*s,.18*s,.16*s,.17*s)] if not chibi else [(1.10,.28,.18,.19),(.97,.48,.31,.20),(.72,.55,.35,.20),(.45,.48,.28,.20),(.30,.25,.17,.18)])
    for side in (-1,1):
        cat_ear(o,side,1)
        eyeX=.19 if not chibi else .27;eyeY=.75 if not chibi else .73
        ellipsoid(o,'eye_white_'+str(side),'white',side*eyeX,eyeY,.482,.107,.071,.026)
        ellipsoid(o,'iris_'+str(side),'eye',side*eyeX,eyeY,.509,.061,.064,.014)
        ellipsoid(o,'pupil_'+str(side),'black',side*eyeX,eyeY,.522,.018,.052,.008)
        ellipsoid(o,'eyelight_'+str(side),'white',side*eyeX-.02,eyeY+.027,.531,.020,.021,.006)
        tube(o,'eyelash_'+str(side),'black',[(side*(eyeX-.10),eyeY+.069,.51),(side*eyeX,eyeY+.078,.51),(side*(eyeX+.10),eyeY+.060,.49)],[.009,.015,.006],8)
        tube(o,'eyebrow_'+str(side),'hairlight',[(side*(eyeX-.09),eyeY+.15,.44),(side*eyeX,eyeY+.16,.45),(side*(eyeX+.09),eyeY+.14,.43)],[.011,.016,.007],8)
        tube(o,'hair_front_'+str(side),'hair',[(side*.33,1.05,.20),(side*.38,.82,.28),(side*.36,.55,.23),(side*.35,.30,.08)],[.09,.095,.068,.008])
        tube(o,'hair_back_'+str(side),'hair',[(side*.25,.97,-.24),(side*.40,.60,-.25),(side*.41,.14,-.25),(side*.37,-.13,-.19)],[.12,.13,.10,.008])
    for i in range(-3,4):
        x=i*.085
        tube(o,'hair_bang_'+str(i),'hairlight' if i%3==0 else 'hair',[(x,1.11,.32),(x*.98,1.00,.47),(x*.95,.88+(abs(i)%2)*.055,.53)],[.068,.076,.005],10)
    ellipsoid(o,'nose','skin',0,.62,.495,.023,.025,.019)
    ellipsoid(o,'mouth','pink',0,.51,.481,.051,.012,.008)
    # Slim shoulder-to-waist silhouette and an uninterrupted dress/skirt surface.
    loft(o,'dress','dress',[(.28,.17,.16,0),(.10,.34,.22,0),(-.20,.38,.25,0),(-.48,.29,.22,0),(-.62,.31,.24,0),(-.78,.42,.32,0),(-.96,.56,.40,0),(-1.10,.61,.43,0),(-1.15,.60,.42,0)],48)
    # Curved apron panel lies on the dress and follows its flare.
    verts=[];faces=[];rows=16;cols=24
    for i in range(rows+1):
        t=i/rows;y=.06-1.20*t;w=.22+.30*t;depth=.23+.27*t
        for j in range(cols+1):
            u=(j/cols-.5)*1.38
            verts.append((w*sin(u),y,depth*cos(u)+.012))
    for i in range(rows):
        for j in range(cols):
            a=i*(cols+1)+j;b=a+1;c=a+cols+1;d=c+1;faces.extend(((a,c,b),(b,c,d)))
    o.mesh('apron','apron',verts,faces)
    tube(o,'hem','apron',[(.60*cos(2*pi*i/64),-1.15+.018*cos(i*pi/4),.42*sin(2*pi*i/64)) for i in range(65)],[.035]*65,8)
    for side in (-1,1):
        # Natural sleeve, wrist, palm, and five distinct fingers.
        tube(o,'sleeve_'+str(side),'dress',[(side*.34,.09,0),(side*.48,-.17,.01),(side*.51,-.40,.035),(side*.57,-.62,.09)],[.16,.16,.11,.075])
        tube(o,'cuff_'+str(side),'apron',[(side*.55,-.60,.09),(side*.57,-.65,.09)],[.088,.084],16)
        ellipsoid(o,'palm_'+str(side),'skin',side*.59,-.75,.12,.077,.13,.039)
        for finger in range(4):
            fx=side*.59+(finger-1.5)*.033
            tube(o,'finger_'+str(side)+'_'+str(finger),'skin',[(fx,-.80,.135),(fx,-.91+(finger%3)*.013,.14)],[.014,.010],8)
        tube(o,'thumb_'+str(side),'skin',[(side*.65,-.76,.15),(side*.71,-.83,.15)],[.020,.010],8)
        tube(o,'leg_'+str(side),'stock',[(side*.19,-1.07,.02),(side*.20,-1.39,.01),(side*.20,-1.73,.02),(side*.20,-2.04,.03)],[.147,.142,.125,.113])
        ellipsoid(o,'shoe_'+str(side),'black',side*.20,-2.10,.13,.155,.095,.24)
    # Chest bow is ribbon shaped instead of two large spherical bumps.
    for side in (-1,1):
        verts=[(0,.02,.32),(side*.19,.09,.33),(side*.17,-.06,.34),(0,-.01,.34)]
        o.mesh('bow_'+str(side),'purple',verts,[(0,1,2),(0,2,3)])
    ellipsoid(o,'bow_knot','apron',0,0,.35,.040,.05,.035)
    tube(o,'tail','hair',[(.48,-.79,-.28),(.63,-.72,-.30),(.77,-.60,-.33),(.89,-.45,-.35),(1.01,-.30,-.34),(1.10,-.17,-.29)],[.11,.115,.10,.09,.075,.028])
    return o

if __name__=='__main__':
    ROOT.mkdir(parents=True,exist_ok=True)
    for chibi,name in ((False,'luna_regular.obj'),(True,'luna_chibi.obj')):
        obj=build(chibi);obj.save(ROOT/name)
        print(name,(ROOT/name).stat().st_size,'bytes')
