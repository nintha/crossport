package none.nintha.crossport.client

import javafx.beans.property.SimpleListProperty
import javafx.beans.property.SimpleStringProperty
import javafx.scene.control.TableView
import none.nintha.crossport.common.MinaConsts
import org.slf4j.LoggerFactory
import tornadofx.*
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection

class MainScreen : View("Crossport 0.0.1") {
    private val logger = LoggerFactory.getLogger(this::class.java)
    private val model = object : ViewModel() {
        val newLocalPort = bind { SimpleStringProperty() }
        val recordList = bind { SimpleListProperty<PortRecord>() }
    }

    init {
        primaryStage.setOnCloseRequest {
            model.recordList.value.forEach { it.remoteClient.close() }
            logger.info("Crossport exit.")
            System.exit(0)
        }
        primaryStage.isResizable = false
    }

    override val root = borderpane {
        top = hbox {
            label("Server Address ${MinaConsts.insideIp}:${MinaConsts.insidePort}") {
                hboxConstraints {
                    marginLeft = 10.0
                    marginTopBottom(5.0)
                }
            }
        }

        center = tableview<PortRecord>(model.recordList) {
            maxHeight = 200.0
            readonlyColumn("Local Port", PortRecord::localPort){minWidth(220.0)}
            readonlyColumn("Remote Address", PortRecord::remotePort) {
                minWidth(220.0)
                cellFormat { text = "${MinaConsts.insideIp}:$it" }
            }
            smartResize()
        }

        bottom = hbox {
            textfield(model.newLocalPort) {
                promptText = "New Local Port"
                hboxConstraints {
                    marginTopBottom(5.0)
                    marginLeft = 10.0
                }
            }
            button("Add") {
                action {
                    val port = model.newLocalPort.value?.toInt() ?: return@action
                    model.newLocalPort.value = ""
                    RemoteClient(port).connect { remoteClient ->
                        {
                            model.recordList.value.add(PortRecord(port, it, remoteClient))
                        }
                    }
                }
                hboxConstraints {
                    marginTopBottom(5.0)
                    marginLeft = 10.0
                }
            }
            button("Remove") {
                action {
                    @Suppress("UNCHECKED_CAST")
                    val selectedItem = (center as TableView<PortRecord>).selectionModel.selectedItem ?: return@action
                    val selectedIndex = (center as TableView<*>).selectionModel.selectedIndex

                    selectedItem.remoteClient.close()
                    model.recordList.value.removeAt(selectedIndex)
                    logger.info("Remove $selectedIndex => $selectedItem")
                }
                hboxConstraints {
                    marginTopBottom(5.0)
                    marginLeft = 10.0
                }
            }
            button("Copy Address") {
                action {
                    @Suppress("UNCHECKED_CAST")
                    val selectedItem = (center as TableView<PortRecord>).selectionModel.selectedItem ?: return@action
                    val address = "${MinaConsts.insideIp}:${selectedItem.remotePort}"
                    val text = StringSelection(address)
                    Toolkit.getDefaultToolkit().systemClipboard.setContents(text, null)
                    logger.info("Copy Address $address")
                }
                hboxConstraints {
                    marginTopBottom(5.0)
                    marginLeft = 10.0
                }
            }
        }
    }

}


