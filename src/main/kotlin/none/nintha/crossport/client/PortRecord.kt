package none.nintha.crossport.client

data class PortRecord(var localPort: Int, var remotePort: Int, val remoteClient: RemoteClient) {

}