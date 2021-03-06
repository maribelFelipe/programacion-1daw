package app;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.TableModel;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

public class GUI extends JFrame {

    private JPanel contentPane;
    private JButton imagenIcon;
    private JTable clientesTable;
    private ClientesTableModel _clientesTableModel;
    private EntityManager _entityManager;
    private JTextField nombreText;
    private JTextField apellidosText;
    private JTable direccionesTable;

    private class DireccionesTableModel implements TableModel {

        private Cliente _cliente;
        private List<Direccion> _direcciones;
        private String[] COLUMNAS = { "Dirección", "C. Postal", "Municipio", "Provincia" };
        private boolean _vacio;

        public DireccionesTableModel(Cliente c) {
            _cliente = c;
            _direcciones = _cliente.getDirecciones();
        }

        public DireccionesTableModel() {
            _vacio = true;
        }

        @Override
        public int getRowCount() {
            if (_vacio)
                return 0;
            return _direcciones.size() + 1;
        }

        @Override
        public int getColumnCount() {
            return COLUMNAS.length;
        }

        @Override
        public String getColumnName(int columnIndex) {
            return COLUMNAS[columnIndex];
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return String.class;
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return !_vacio;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            if (rowIndex == getRowCount() - 1) {
                return "Añadir...";
            }
            Direccion d = _direcciones.get(rowIndex);
            switch (columnIndex) {
            case 0:
                return d.getDireccion();
            case 1:
                return d.getCodigoPostal();
            case 2:
                return d.getMunicipio();
            case 3:
                return d.getProvincia();
            default:
                return "???";
            }
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            if (rowIndex == getRowCount() - 1) {
                Direccion d = new Direccion();
                addDireccion(d);
            }
            String v = aValue.toString();
            Direccion d = _direcciones.get(rowIndex);
            switch (columnIndex) {
            case 0:
                d.setDireccion(v);
                break;
            case 1:
                d.setCodigoPostal(v);
                break;
            case 2:
                d.setMunicipio(v);
                break;
            case 3:
                d.setProvincia(v);
                break;
            default:
                throw new IllegalArgumentException();
            }
            salvarDireccion(d);
            fireTableChanged(new TableModelEvent(this));
        }

        private void addDireccion(Direccion d) {
            d.setCliente(_cliente);
            salvarCliente(_cliente);
            salvarDireccion(d);
        }

        private List<TableModelListener> _listeners = new ArrayList<TableModelListener>();

        private void fireTableChanged(TableModelEvent e) {
            for (TableModelListener l : _listeners) {
                l.tableChanged(e);
            }
        }

        @Override
        public void addTableModelListener(TableModelListener l) {
            _listeners.add(l);
        }

        @Override
        public void removeTableModelListener(TableModelListener l) {
            _listeners.remove(l);
        }

    }

    private class ClientesTableModel implements TableModel {

        private List<Cliente> _clientes;
        private String[] _columnNames = { "ID", "Nombre", "Apellidos" };
        private List<TableModelListener> _listeners = new ArrayList<TableModelListener>();

        public ClientesTableModel() {
            _clientes = leerClientes();
        }

        private List<Cliente> leerClientes() {
            EntityTransaction tx = getEntityManager().getTransaction();
            try {
                tx.begin();
                Query query = getEntityManager().createQuery("FROM Cliente");
                @SuppressWarnings("unchecked")
                List<Cliente> ret = query.getResultList();
                return ret;
            }
            finally {
                tx.commit();
            }

        }

        @Override
        public int getRowCount() {
            return _clientes.size() + 1;
        }

        @Override
        public int getColumnCount() {
            return _columnNames.length;
        }

        @Override
        public String getColumnName(int columnIndex) {
            return _columnNames[columnIndex];
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return String.class;
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return !getColumnName(columnIndex).equals("ID");
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            if (rowIndex == getRowCount() - 1) {
                return "Añadir...";
            }
            Cliente c = _clientes.get(rowIndex);
            String column = getColumnName(columnIndex);
            if (column.equals("ID"))
                return "" + c.getIdCliente();
            if (column.equals("Nombre"))
                return c.getNombre();
            if (column.equals("Apellidos"))
                return c.getApellidos();
            return "???";
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            if (rowIndex == getRowCount() - 1) {
                Cliente c = new Cliente();
                _clientes.add(c);
            }
            String value = aValue.toString();
            Cliente c = _clientes.get(rowIndex);
            String column = getColumnName(columnIndex);
            if (column.equals("Nombre"))
                c.setNombre(value);
            if (column.equals("Apellidos"))
                c.setApellidos(value);

            salvarCliente(c);
            fireTableChanged(rowIndex);
        }

        public void fireTableChanged(int rowIndex) {
            TableModelEvent e = new TableModelEvent(this, rowIndex);
            fireTableChanged(e);
        }

        private void fireTableChanged(TableModelEvent e) {
            for (TableModelListener l : _listeners) {
                l.tableChanged(e);
            }
        }

        @Override
        public void addTableModelListener(TableModelListener l) {
            _listeners.add(l);
        }

        @Override
        public void removeTableModelListener(TableModelListener l) {
            _listeners.remove(l);
        }

        public Cliente getCliente(int row) {
            if (row > _clientes.size() - 1) {
                return null;
            }
            return _clientes.get(row);
        }

    }

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    GUI frame = new GUI();
                    frame.setVisible(true);
                    frame.inicializaBaseDeDatos();
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    protected void inicializaBaseDeDatos() {
        clientesTable.setModel(getClientesTableModel());
    }

    /**
     * Create the frame.
     */
    public GUI() {
        initComponents();
        initListeners();
    }

    private void initComponents() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 781, 435);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        contentPane.setLayout(new BorderLayout(0, 0));
        setContentPane(contentPane);

        JPanel clientesPanel = new JPanel();
        contentPane.add(clientesPanel, BorderLayout.CENTER);
        clientesPanel.setLayout(new BorderLayout(0, 0));

        JScrollPane scrollPane = new JScrollPane();
        clientesPanel.add(scrollPane, BorderLayout.CENTER);

        clientesTable = new JTable();
        scrollPane.setViewportView(clientesTable);

        JPanel datosCliente = new JPanel();

        clientesPanel.add(datosCliente, BorderLayout.SOUTH);

        GridBagLayout gbl_datosCliente = new GridBagLayout();
        gbl_datosCliente.columnWeights = new double[] { 1.0, 0.0 };

        datosCliente.setLayout(gbl_datosCliente);

        JPanel datosTextoCliente = new JPanel();
        GridBagConstraints gbc_datosTextoCliente = new GridBagConstraints();
        gbc_datosTextoCliente.gridheight = 2;
        gbc_datosTextoCliente.weighty = 1.0;
        gbc_datosTextoCliente.weightx = 1.0;
        gbc_datosTextoCliente.insets = new Insets(0, 0, 5, 5);
        gbc_datosTextoCliente.fill = GridBagConstraints.BOTH;
        gbc_datosTextoCliente.gridx = 0;
        gbc_datosTextoCliente.gridy = 0;
        datosCliente.add(datosTextoCliente, gbc_datosTextoCliente);
        GridBagLayout gbl_datosTextoCliente = new GridBagLayout();
        gbl_datosTextoCliente.columnWidths = new int[] { 0, 0, 0 };
        gbl_datosTextoCliente.rowHeights = new int[] { 0, 0, 0, 0 };
        gbl_datosTextoCliente.columnWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
        gbl_datosTextoCliente.rowWeights = new double[] { 0.0, 0.0, 1.0, Double.MIN_VALUE };
        datosTextoCliente.setLayout(gbl_datosTextoCliente);

        JLabel lblNewLabel = new JLabel("Nombre");
        GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
        gbc_lblNewLabel.anchor = GridBagConstraints.EAST;
        gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
        gbc_lblNewLabel.gridx = 0;
        gbc_lblNewLabel.gridy = 0;
        datosTextoCliente.add(lblNewLabel, gbc_lblNewLabel);

        nombreText = new JTextField();
        nombreText.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                textosCambiados();
            }
        });
        GridBagConstraints gbc_nombreText = new GridBagConstraints();
        gbc_nombreText.weightx = 1.0;
        gbc_nombreText.insets = new Insets(0, 0, 5, 0);
        gbc_nombreText.fill = GridBagConstraints.HORIZONTAL;
        gbc_nombreText.gridx = 1;
        gbc_nombreText.gridy = 0;
        datosTextoCliente.add(nombreText, gbc_nombreText);
        nombreText.setColumns(10);

        JLabel lblApellidos = new JLabel("Apellidos");
        GridBagConstraints gbc_lblApellidos = new GridBagConstraints();
        gbc_lblApellidos.anchor = GridBagConstraints.EAST;
        gbc_lblApellidos.insets = new Insets(0, 0, 5, 5);
        gbc_lblApellidos.gridx = 0;
        gbc_lblApellidos.gridy = 1;
        datosTextoCliente.add(lblApellidos, gbc_lblApellidos);

        apellidosText = new JTextField();
        apellidosText.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                textosCambiados();
            }
        });
        GridBagConstraints gbc_apellidosText = new GridBagConstraints();
        gbc_apellidosText.insets = new Insets(0, 0, 5, 0);
        gbc_apellidosText.fill = GridBagConstraints.HORIZONTAL;
        gbc_apellidosText.gridx = 1;
        gbc_apellidosText.gridy = 1;
        datosTextoCliente.add(apellidosText, gbc_apellidosText);
        apellidosText.setColumns(10);

        JLabel lblDirecciones = new JLabel("Direcciones");
        GridBagConstraints gbc_lblDirecciones = new GridBagConstraints();
        gbc_lblDirecciones.insets = new Insets(0, 0, 0, 5);
        gbc_lblDirecciones.gridx = 0;
        gbc_lblDirecciones.gridy = 2;
        datosTextoCliente.add(lblDirecciones, gbc_lblDirecciones);

        JScrollPane scrollPane_1 = new JScrollPane();
        GridBagConstraints gbc_scrollPane_1 = new GridBagConstraints();
        gbc_scrollPane_1.weighty = 1.0;
        gbc_scrollPane_1.fill = GridBagConstraints.BOTH;
        gbc_scrollPane_1.gridx = 1;
        gbc_scrollPane_1.gridy = 2;
        datosTextoCliente.add(scrollPane_1, gbc_scrollPane_1);

        direccionesTable = new JTable();
        direccionesTable.setPreferredScrollableViewportSize(new Dimension(100, 100));
        scrollPane_1.setViewportView(direccionesTable);

        JPanel panelConImagen = new JPanel();
        GridBagConstraints gbc_panelConImagen = new GridBagConstraints();
        gbc_panelConImagen.anchor = GridBagConstraints.NORTH;
        gbc_panelConImagen.insets = new Insets(0, 0, 5, 5);
        gbc_panelConImagen.gridx = 1;
        gbc_panelConImagen.gridy = 0;
        datosCliente.add(panelConImagen, gbc_panelConImagen);
        GridBagLayout gbl_panelConImagen = new GridBagLayout();
        gbl_panelConImagen.columnWeights = new double[] { 0.0 };
        gbl_panelConImagen.rowWeights = new double[] { 0.0 };
        panelConImagen.setLayout(gbl_panelConImagen);

        imagenIcon = new JButton("");
        GridBagConstraints gbc_imagenIcon = new GridBagConstraints();
        gbc_imagenIcon.anchor = GridBagConstraints.NORTH;
        gbc_imagenIcon.insets = new Insets(0, 0, 5, 5);
        gbc_imagenIcon.gridx = 0;
        gbc_imagenIcon.gridy = 0;
        panelConImagen.add(imagenIcon, gbc_imagenIcon);
        imagenIcon.setPreferredSize(new Dimension(256, 256));
        imagenIcon.setMinimumSize(new Dimension(256, 256));
        imagenIcon.setMaximumSize(new Dimension(256, 256));
        imagenIcon.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cambiarImagen();
            }
        });

    }

    protected void textosCambiados() {
        System.out.println("textosCambiados");
        Cliente c = getClienteSeleccionado();
        if (c != null) {
            System.out.println("  id:" + c.getIdCliente());
        }
        if (c == null) {
            return;
        }
        c.setApellidos(apellidosText.getText());
        c.setNombre(nombreText.getText());
        salvarCliente(c);
        actualizarTablaCliente();
    }

    private void actualizarTablaCliente() {
        int selectedRow = clientesTable.getSelectedRow();
        _clientesTableModel.fireTableChanged(selectedRow);
    }

    private class IconoAjustadoAComponente implements Icon {

        private Image _image;
        private int _w;
        private int _h;

        public IconoAjustadoAComponente(int w, int h, byte[] imageData) {
            _w = w;
            _h = h;
            _image = Toolkit.getDefaultToolkit().createImage(imageData);
            loadImage(_image);
        }

        protected void loadImage(Image image) {
            MediaTracker mTracker = getTracker();
            synchronized (mTracker) {
                int id = (int) (Math.random() * 10000);

                mTracker.addImage(image, id);
                try {
                    mTracker.waitForID(id, 0);
                }
                catch (InterruptedException e) {
                    System.out.println("INTERRUPTED while loading Image");
                }
                mTracker.removeImage(image, id);

            }
        }

        private MediaTracker getTracker() {
            Component comp = GUI.this;
            return new MediaTracker(comp);
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            int w = c.getWidth() - _w;
            int h = c.getHeight() - _h;
            g.drawImage(_image, 0, 0, w, h, null);

        }

        @Override
        public int getIconWidth() {
            return _w;
        }

        @Override
        public int getIconHeight() {
            return _h;
        }

    }

    private void initListeners() {
        clientesTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {
                Cliente c = getClienteSeleccionado();

                cambioClienteSeleccionado(c);
            }
        });
    }

    protected void cambiarImagen() {
        Cliente c = getClienteSeleccionado();
        if (c == null) {
            return;
        }
        JFileChooser jfc = new JFileChooser();
        jfc.setFileFilter(new FileNameExtensionFilter("Imágenes", "png", "jpg", "gif", "jpeg", "bmp"));
        int ret = jfc.showOpenDialog(this);
        if (ret != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File f = jfc.getSelectedFile();
        setImage(c, f);
        salvarCliente(c);
    }

    private void setImage(Cliente c, File f) {
        try {
            InputStream in = new FileInputStream(f);
            byte buffer[] = new byte[1024];
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int leidos = in.read(buffer);
            while (leidos > 0) {
                baos.write(buffer, 0, leidos);
                leidos = in.read(buffer);
            }
            in.close();
            baos.close();
            c.setImagen(baos.toByteArray());
            Icon icon = new IconoAjustadoAComponente(4, 4, c.getImagen());
            imagenIcon.setIcon(icon);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Cliente getClienteSeleccionado() {
        int selectedRow = clientesTable.getSelectedRow();
        if (selectedRow == -1) {
            return null;
        }
        return _clientesTableModel.getCliente(selectedRow);
    }

    private ClientesTableModel getClientesTableModel() {
        if (_clientesTableModel == null) {
            _clientesTableModel = new ClientesTableModel();

        }

        return _clientesTableModel;
    }

    private EntityManager getEntityManager() {
        if (_entityManager == null) {
            EntityManagerFactory emf = Persistence.createEntityManagerFactory("ejemplo-derby");
            _entityManager = emf.createEntityManager();
        }
        return _entityManager;
    }

    private void salvarCliente(Cliente c) {
        salvarObjeto(c);
    }

    private void salvarObjeto(Object o) {
        EntityTransaction tx = getEntityManager().getTransaction();
        try {
            tx.begin();
            getEntityManager().persist(o);
        }
        finally {
            tx.commit();
        }
    }

    private void salvarDireccion(Direccion d) {
        salvarObjeto(d);
    }

    private void cambioClienteSeleccionado(Cliente c) {
        System.out.println("cambioClienteSeleccionado:");
        if (c != null) {
            System.out.println("  id:" + c.getIdCliente());
        }
        if (c == null || c.getImagen() == null) {
            imagenIcon.setIcon(null);
        }
        else {
            Icon icon = new IconoAjustadoAComponente(4, 4, c.getImagen());
            imagenIcon.setIcon(icon);
        }

        if (c == null) {
            direccionesTable.setModel(new DireccionesTableModel());
        }
        else {
            direccionesTable.setModel(new DireccionesTableModel(c));
        }

        if (c == null) {
            nombreText.setText("");
            apellidosText.setText("");
        }
        else {
            nombreText.setText(c.getNombre());
            apellidosText.setText(c.getApellidos());
        }
        nombreText.setEnabled(c != null);
        apellidosText.setEnabled(c != null);

    }

}
