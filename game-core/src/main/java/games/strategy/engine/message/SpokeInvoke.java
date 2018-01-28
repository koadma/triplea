package games.strategy.engine.message;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import games.strategy.engine.message.unifiedmessenger.Invoke;
import games.strategy.net.GUID;
import games.strategy.net.INode;
import games.strategy.net.Node;

public class SpokeInvoke extends Invoke {
  private static final long serialVersionUID = -2007645463748969L;
  private INode invoker;

  public SpokeInvoke() {
    super();
  }

  public SpokeInvoke(final GUID methodCallId, final boolean needReturnValues, final RemoteMethodCall call,
      final INode invoker) {
    super(methodCallId, needReturnValues, call);
    this.invoker = invoker;
  }

  public INode getInvoker() {
    return invoker;
  }

  @Override
  public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
    super.readExternal(in);
    invoker = new Node();
    ((Node) invoker).readExternal(in);
  }

  @Override
  public void writeExternal(final ObjectOutput out) throws IOException {
    super.writeExternal(out);
    ((Node) invoker).writeExternal(out);
  }
}
