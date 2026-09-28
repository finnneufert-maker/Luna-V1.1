"""Authoring source for Luna's editable OBJ models. Run with Python 3 (stdlib only)."""
from math import cos, sin, pi, sqrt
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1] / 'app/src/main/assets'

class Obj:
    def __init__(self,chibi=False):
        self.lines=['# Luna Yamanaka: sculptable mesh asset. Coordinates: Y up, Z front.']
        self.chibi=chibi
        self.count=0
        self.normal_count=0
    def mesh(self,name,mat,vertices,faces):
        if self.chibi and not (name.startswith(('hair','face','ear','eye','iris','pupil','eyelash','eyebrow','nose','mouth'))):
            # Shorter limbs and skirt, while the oversized head keeps its own shape.
            vertices=[(x*.86, .28+(y-.28)*.72, z*.89) for x,y,z in vertices]
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

def ellipsoid(obj,name,mat,x,y,z,rx,ry,rz,rows=14,sides=20):
    surface(obj,name,mat,rows,sides,lambda t,a:(x+rx*sin(pi*t)*cos(a),y+ry*cos(pi*t),z+rz*sin(pi*t)*sin(a)))

def loft(obj,name,mat,rings,steps=28):
    # Hand-defined radial cross sections: (Y, width, depth, Z center).
    def fn(t,a):
        at=t*(len(rings)-1);i=min(len(rings)-2,int(at));u=at-i
        # Cubic interpolation avoids the flat ridges from stopping at each ring.
        values=[]
        for k in range(4):
            p0=rings[max(0,i-1)][k];p1=rings[i][k]
            p2=rings[i+1][k];p3=rings[min(len(rings)-1,i+2)][k]
            values.append(.5*((2*p1)+(-p0+p2)*u+(2*p0-5*p1+4*p2-p3)*u*u+(-p0+3*p1-3*p2+p3)*u*u*u))
        y,w,d,z=values
        if name=='dress' and y<-.65:
            pleat=1+.025*cos(10*a)*min(1,(-y-.65)*2)
            w*=pleat;d*=pleat
        return (w*cos(a),y,z+d*sin(a))
    if name == 'face':
        # Skin is the front of the head, not a second closed head around the hair.
        # A closed loft exposed broad flesh-colored side patches in profile.
        rows=(len(rings)-1)*5+1
        vertices=[fn(i/(rows-1),.30+(pi-.60)*j/steps)
                  for i in range(rows) for j in range(steps+1)]
        faces=[]
        for i in range(rows-1):
            for j in range(steps):
                a=i*(steps+1)+j;b=a+1;c=a+steps+1;d=c+1
                faces.extend(((a,b,c),(b,d,c)))
        obj.mesh(name,mat,vertices,faces)
    else:
        surface(obj,name,mat,(len(rings)-1)*5+1,steps,fn,True)

def tube(obj,name,mat,points,radii,sides=10):
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

def cat_ear(obj,side,scale,chibi=False):
    x=side*(.38 if chibi else .29)*scale;base=1.08*scale;z=-.025*scale
    def shape(t,a):
        width=(.17*(1-t*t)**.7+.018)*scale;depth=(.10*(1-t)+.018)*scale
        return (x+side*.07*t*scale+width*cos(a),base+.34*t*scale,z+.03*t*scale+depth*sin(a))
    surface(obj,'ear_'+str(side),'hair',9,20,shape)
    # Pink inner ear follows the forward face, with a rounded top.
    def inner(t,a):
        width=(.105*(1-t*t)**.7+.004)*scale
        return (x+side*.07*t*scale+width*cos(a),base+(.035+.25*t)*scale,(.105+.025*t)*scale+.006*scale*sin(a))
    surface(obj,'ear_inner_'+str(side),'pink',8,16,inner)

def build(chibi=False):
    o=Obj(chibi);s=1 if not chibi else .94
    # A narrowed jaw and continuous back of head, with silver hair behind it.
    ellipsoid(o,'hair_shell','hair',0,.69*s,-.055*s,.53 if chibi else .39*s,.49*s,.37*s)
    loft(o,'face','skin',[(1.11*s,.17*s,.12*s,.14*s),(1.01*s,.28*s,.20*s,.17*s),(.85*s,.32*s,.27*s,.20*s),(.65*s,.33*s,.29*s,.20*s),(.46*s,.29*s,.24*s,.18*s),(.32*s,.20*s,.16*s,.15*s),(.27*s,.08*s,.08*s,.14*s)] if not chibi else [(1.11,.20,.13,.15),(1.00,.36,.23,.18),(.82,.44,.31,.20),(.63,.46,.33,.20),(.45,.42,.27,.18),(.33,.28,.18,.16),(.29,.10,.09,.14)])
    for side in (-1,1):
        cat_ear(o,side,1,chibi)
        eyeX=.19 if not chibi else .27;eyeY=.75 if not chibi else .73
        eyeDepth=.055 if chibi else 0
        ellipsoid(o,'eye_white_'+str(side),'white',side*eyeX,eyeY,.482+eyeDepth,.107,.071,.020)
        ellipsoid(o,'iris_'+str(side),'eye',side*eyeX,eyeY,.503+eyeDepth,.061,.064,.012)
        ellipsoid(o,'pupil_'+str(side),'black',side*eyeX,eyeY,.515+eyeDepth,.018,.052,.007)
        ellipsoid(o,'eyelight_'+str(side),'white',side*eyeX-.02,eyeY+.027,.523+eyeDepth,.020,.021,.005)
        tube(o,'eyelash_'+str(side),'black',[(side*(eyeX-.10),eyeY+.069,.505+eyeDepth),(side*eyeX,eyeY+.078,.505+eyeDepth),(side*(eyeX+.10),eyeY+.060,.485+eyeDepth)],[.009,.015,.006],8)
        tube(o,'eyebrow_'+str(side),'hairlight',[(side*(eyeX-.09),eyeY+.15,.44+eyeDepth),(side*eyeX,eyeY+.16,.45+eyeDepth),(side*(eyeX+.09),eyeY+.14,.43+eyeDepth)],[.011,.016,.007],8)
        hairX=.47 if chibi else .34
        tube(o,'hair_front_'+str(side),'hair',[(side*hairX,1.05,.12),(side*(hairX+.025),.82,.13),(side*(hairX+.025),.55,.12),(side*hairX,.30,.04)],[.085,.09,.065,.008])
        # Cover the exposed side of the face; Luna only has the cat ears above her head.
        tube(o,'hair_temple_'+str(side),'hair',[(side*(hairX-.03),1.00,.14),(side*(hairX+.005),.82,.20),(side*(hairX+.015),.61,.19),(side*(hairX+.01),.39,.15)],
             [.055,.085,.08,.008],12)
        tube(o,'hair_back_'+str(side),'hair',[(side*(hairX-.07),.97,-.25),(side*(hairX+.02),.60,-.25),(side*(hairX+.03),.14,-.25),(side*hairX,-.13,-.19)],[.12,.13,.10,.008])
    # Broad overlapping locks grow from the crown, then taper into curved bangs.
    # Their staggered tips keep the forehead readable without a row of spikes.
    for i in range(-4,5):
        x=i*.078
        sweep=.018*sin(i*1.4)
        tip=.91+.035*(abs(i)%3)
        tube(o,'hair_crown_'+str(i),'hairlight' if i in (-3,2) else 'hair',
             [(x*.73,1.16,-.07),(x*.9,1.17,.15),(x+sweep,1.10,.34),
              (x+sweep*.6,1.01,.45)],
             [.078,.092,.096,.078],12)
        tube(o,'hair_bang_'+str(i),'hairlight' if i in (-3,2) else 'hair',
             [(x+sweep*.6,1.04,.43),(x+sweep,.98,.49),
              (x+sweep+.018*sin(i),tip,.515)],
             [.072,.078,.004],12)
    for i in range(-2,3):
        x=i*.115
        tube(o,'hair_fine_bang_'+str(i),'hairlight',
             [(x,1.05,.46),(x+.008*sin(i),.99,.518),(x+.012*sin(i),.925+(abs(i)%2)*.025,.527)],
             [.005,.006,.001],6)
    # Layered long hair remains visible from behind, down to the waist.
    for i in range(-3,4):
        x=i*(.11 if chibi else .09)
        tip_y=-.56-.035*(abs(i)%3)
        tube(o,'hair_rear_'+str(i),'hairlight' if i in (-2,1) else 'hair',
             [(x*.75,1.01,-.29),(x,.63,-.37),(x*1.08,.20,-.39),
              (x*1.10+.018*sin(i),-.20,-.38),(x*1.04+.028*sin(i),tip_y,-.34)],
             [.055,.087,.095,.07,.004],10)
    for i in range(-3,4):
        x=i*(.09 if chibi else .077)
        tube(o,'hair_fine_rear_'+str(i),'hairlight' if i in (-2,1,3) else 'hair',
             [(x,.83,-.395),(x+.01*sin(i),.43,-.468),(x+.02*sin(i),.02,-.49),
              (x+.033*sin(i),-.30-.045*(abs(i)%3),-.445)],
             [.004,.007,.008,.002],6)
    # The bridge and shallow triangular cat nose read as a nose in profile.
    nose_z=.030 if chibi else 0
    tube(o,'nose_bridge','skin',[(0,.70,.475+nose_z),(0,.65,.493+nose_z),(0,.60,.512+nose_z)],
         [.013,.018,.021],10)
    o.mesh('nose_tip','skin',[(0,.604,.517+nose_z),(-.022,.585,.522+nose_z),
                              (.022,.585,.522+nose_z),(0,.581,.535+nose_z)],
           [(0,1,3),(0,3,2),(1,2,3)])
    for side in (-1,1):
        ellipsoid(o,'nose_nostril_'+str(side),'pink',side*.016,.583,.534+nose_z,.005,.003,.002,7,10)
    ellipsoid(o,'mouth','pink',0,.51,.54 if chibi else .481,.051,.012,.008)
    # Slim shoulder-to-waist silhouette and an uninterrupted dress/skirt surface.
    loft(o,'dress','dress',[(.28,.17,.16,0),(.10,.34,.22,0),(-.20,.38,.25,0),(-.48,.29,.22,0),(-.62,.31,.24,0),(-.78,.42,.32,0),(-.96,.56,.40,0),(-1.10,.61,.43,0),(-1.15,.60,.42,0)],32)
    # Curved apron panel lies on the dress and follows its flare.
    verts=[];faces=[];rows=12;cols=16
    for i in range(rows+1):
        t=i/rows;y=.06-1.20*t;w=.22+.30*t;depth=.23+.27*t
        for j in range(cols+1):
            u=(j/cols-.5)*1.38
            verts.append((w*sin(u),y,depth*cos(u)+.012))
    for i in range(rows):
        for j in range(cols):
            a=i*(cols+1)+j;b=a+1;c=a+cols+1;d=c+1;faces.extend(((a,c,b),(b,c,d)))
    o.mesh('apron','apron',verts,faces)
    for side in (-1,1):
        tube(o,'apron_strap_'+str(side),'apron',
             [(side*.21,.18,.17),(side*.24,.04,.235),(side*.25,-.18,.29),(side*.23,-.37,.315)],
             [.033,.039,.035,.025],10)
    # A small waist bow gives the back of the dress a separate silhouette.
    for side in (-1,1):
        o.mesh('back_bow_'+str(side),'apron',
               [(0,-.48,-.265),(side*.24,-.38,-.32),(side*.20,-.62,-.33)],[(0,1,2)])
    ellipsoid(o,'back_bow_knot','purple',0,-.49,-.34,.045,.055,.027)
    tube(o,'hem','apron',[(.60*cos(2*pi*i/32),-1.15+.018*cos(i*pi/4),.42*sin(2*pi*i/32)) for i in range(33)],[.035]*33,8)
    for side in (-1,1):
        # Natural sleeve, wrist, palm, and five distinct fingers.
        tube(o,'sleeve_'+str(side),'dress',[(side*.34,.09,0),(side*.48,-.17,.01),(side*.51,-.40,.035),(side*.57,-.62,.09)],[.16,.16,.11,.075])
        tube(o,'cuff_'+str(side),'apron',[(side*.55,-.60,.09),(side*.57,-.65,.09)],[.088,.084],16)
        ellipsoid(o,'palm_'+str(side),'skin',side*.59,-.75,.12,.077,.13,.039)
        for finger in range(4):
            fx=side*.59+(finger-1.5)*.037
            length=(.088,.115,.107,.080)[finger]
            tube(o,'finger_'+str(side)+'_'+str(finger),'skin',
                 [(fx,-.805,.137),(fx+(finger-1.5)*.003,-.805-length*.56,.146),
                  (fx+(finger-1.5)*.006,-.805-length,.149)],
                 [.017,.015,.011],10)
            ellipsoid(o,'nail_'+str(side)+'_'+str(finger),'nail',
                      fx+(finger-1.5)*.006,-.805-length+.015,.162,.012,.020,.004,7,10)
        tube(o,'thumb_'+str(side),'skin',[(side*.65,-.755,.15),(side*.69,-.80,.157),
                                          (side*.71,-.84,.16)],[.022,.019,.012],10)
        ellipsoid(o,'nail_thumb_'+str(side),'nail',side*.71,-.83,.174,.010,.016,.004,7,10)
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
