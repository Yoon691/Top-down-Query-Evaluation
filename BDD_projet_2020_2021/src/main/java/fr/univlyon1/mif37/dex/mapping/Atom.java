package fr.univlyon1.mif37.dex.mapping;

import fr.univlyon1.mif37.dex.mapping.topDown.Tuple;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;

public class Atom {

    private String name;
    private Value[] args;
    private  List<Boolean> adornment;
    private Boolean isGround;

    public Atom(String name, List<Value> args) {
        this.name = name;
        this.args = args.toArray(new Value[args.size()]);

    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Value[] getArgs() {
        return args;
    }

    public Collection<Variable> getVars() {
      Collection<Variable> container = new ArrayList<Variable>();
      for (Value v: this.getArgs()) {
        if (v instanceof Variable) {
          container.add((Variable)v);
        }
      }
      return container;
    }
    public static Atom create(final String pred, final Value[] args) {
        return new Atom(pred, Arrays.asList(args));
    }

    public Constant unify(Tuple fact) {
        assert fact.isGround();
        if (!this.getName().equals(fact.name)) {
            return null;
        }
        return Constant.parse(this.name);
    }

    public boolean isGround() {
        Boolean isGround;
        if ((isGround = this.isGround) == null) {
            // This might do redundant work since we do not synchronize, but
            // it's still sound, and it's probably cheap enough that
            // synchronizing might be more expensive.
            boolean b = true;
            for (Value t : args) {
                b &= t instanceof Constant;
            }
            this.isGround = isGround = Boolean.valueOf(b);
        }
        return isGround;
    }
    @Override
    public String toString() {
        String result = "";
        result += name + "(";
        int i = 1;
        for(Value v : args) {
            result += v;
            if(i  != args.length)
                result += ",";
            ++i;
        }
        result += ")";
        return  result;
    }

    public List<Boolean> getAdornment() {
        return adornment;
    }
}
