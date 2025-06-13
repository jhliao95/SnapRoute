import React, { useState, useEffect } from 'react';
import { 
  AppBar, 
  Toolbar, 
  Typography, 
  Container, 
  Card, 
  CardContent, 
  Button,
  Box,
  Stack,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  Alert,
  Snackbar
} from '@mui/material';
import { AdapterDateFns } from '@mui/x-date-pickers/AdapterDateFns';
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider';
import { DatePicker } from '@mui/x-date-pickers/DatePicker';
import AddIcon from '@mui/icons-material/Add';
import zhCN from 'date-fns/locale/zh-CN';
import { format } from 'date-fns';
import { tripApi, Trip } from './services/api';

interface TripFormData {
  title: string;
  date: Date | null;
  description: string;
}

function App() {
  const [open, setOpen] = useState(false);
  const [trips, setTrips] = useState<Trip[]>([]);
  const [formData, setFormData] = useState<TripFormData>({
    title: '',
    date: new Date(),
    description: ''
  });
  const [snackbar, setSnackbar] = useState({
    open: false,
    message: '',
    severity: 'success' as 'success' | 'error'
  });

  // 加载所有行程
  const loadTrips = async () => {
    try {
      const data = await tripApi.getAllTrips();
      setTrips(data);
    } catch (error) {
      console.error('加载行程失败:', error);
      setSnackbar({
        open: true,
        message: '加载行程失败',
        severity: 'error'
      });
    }
  };

  // 组件加载时获取行程数据
  useEffect(() => {
    loadTrips();
  }, []);

  const handleClickOpen = () => {
    setOpen(true);
  };

  const handleClose = () => {
    setOpen(false);
    // 重置表单
    setFormData({
      title: '',
      date: new Date(),
      description: ''
    });
  };

  const handleSubmit = async () => {
    try {
      if (!formData.date) {
        throw new Error('请选择日期');
      }

      await tripApi.createTrip({
        title: formData.title,
        date: format(formData.date, 'yyyy-MM-dd'),
        description: formData.description
      });

      setSnackbar({
        open: true,
        message: '保存成功',
        severity: 'success'
      });
      
      handleClose();
      loadTrips(); // 重新加载行程列表
    } catch (error) {
      console.error('保存失败:', error);
      setSnackbar({
        open: true,
        message: '保存失败',
        severity: 'error'
      });
    }
  };

  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', minHeight: '100vh' }}>
      <AppBar position="static">
        <Toolbar>
          <Typography variant="h6">
            SnapRoute - 我的行程记录
          </Typography>
        </Toolbar>
      </AppBar>

      <Container sx={{ mt: 4, flex: 1 }}>
        <Stack spacing={3}>
          <Button 
            variant="contained" 
            startIcon={<AddIcon />}
            size="large"
            sx={{ alignSelf: 'flex-start' }}
            onClick={handleClickOpen}
          >
            添加新行程
          </Button>

          {trips.map((trip) => (
            <Card key={trip.id}>
              <CardContent>
                <Typography variant="h6" gutterBottom>
                  {trip.title}
                </Typography>
                <Typography color="text.secondary">
                  {format(new Date(trip.date), 'yyyy年MM月dd日')}
                </Typography>
                <Typography variant="body2" sx={{ mt: 1 }}>
                  {trip.description}
                </Typography>
              </CardContent>
            </Card>
          ))}
        </Stack>
      </Container>

      <Dialog open={open} onClose={handleClose} maxWidth="sm" fullWidth>
        <DialogTitle>添加新行程</DialogTitle>
        <DialogContent>
          <Stack spacing={3} sx={{ mt: 1 }}>
            <TextField
              label="行程标题"
              fullWidth
              value={formData.title}
              onChange={(e) => setFormData({ ...formData, title: e.target.value })}
            />
            <LocalizationProvider dateAdapter={AdapterDateFns} adapterLocale={zhCN}>
              <DatePicker
                label="日期"
                value={formData.date}
                onChange={(newValue) => {
                  setFormData({ ...formData, date: newValue });
                }}
                slotProps={{ textField: { fullWidth: true } }}
              />
            </LocalizationProvider>
            <TextField
              label="行程描述"
              fullWidth
              multiline
              rows={4}
              value={formData.description}
              onChange={(e) => setFormData({ ...formData, description: e.target.value })}
            />
          </Stack>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleClose}>取消</Button>
          <Button onClick={handleSubmit} variant="contained">
            保存
          </Button>
        </DialogActions>
      </Dialog>

      <Snackbar
        open={snackbar.open}
        autoHideDuration={6000}
        onClose={() => setSnackbar({ ...snackbar, open: false })}
      >
        <Alert 
          onClose={() => setSnackbar({ ...snackbar, open: false })} 
          severity={snackbar.severity}
        >
          {snackbar.message}
        </Alert>
      </Snackbar>
    </Box>
  );
}

export default App;
