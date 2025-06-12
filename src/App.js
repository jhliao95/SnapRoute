import React, { useState, useEffect } from 'react';
import { 
  Button, 
  Container, 
  Box, 
  Typography,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  Paper,
  Grid,
  Card,
  CardContent,
  CardMedia,
  CardActions,
  Divider,
  Stack
} from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import PhotoCamera from '@mui/icons-material/PhotoCamera';
import ListAltIcon from '@mui/icons-material/ListAlt';
import EXIF from 'exif-js';

function App() {
  const [open, setOpen] = useState(false);
  const [tripsDialogOpen, setTripsDialogOpen] = useState(false);
  const [tripData, setTripData] = useState({
    destination: '',
    startDate: '',
    endDate: '',
    notes: '',
    photos: []
  });
  const [trips, setTrips] = useState([]);
  const [previewUrls, setPreviewUrls] = useState([]);

  // 从本地存储加载行程数据
  useEffect(() => {
    const savedTrips = localStorage.getItem('trips');
    if (savedTrips) {
      setTrips(JSON.parse(savedTrips));
    }
  }, []);

  const handleClickOpen = () => {
    setOpen(true);
  };

  const handleClose = () => {
    setOpen(false);
    setPreviewUrls([]);
    setTripData({
      destination: '',
      startDate: '',
      endDate: '',
      notes: '',
      photos: []
    });
  };

  const handleSubmit = async () => {
    try {
      const formData = new FormData();
      formData.append('destination', tripData.destination);
      formData.append('startDate', tripData.startDate);
      formData.append('endDate', tripData.endDate);
      formData.append('notes', tripData.notes);
      
      tripData.photos.forEach((photo, index) => {
        formData.append(`photos`, photo.file);
      });

      const response = await fetch('/api/trips', {
        method: 'POST',
        body: formData,
      });

      if (!response.ok) {
        throw new Error('Failed to save trip');
      }

      const newTrip = {
        ...tripData,
        id: Date.now(),
        createdAt: new Date().toISOString(),
        photos: previewUrls.map((url, index) => ({
          url,
          exifData: tripData.photos[index]?.exifData || {}
        }))
      };

      const updatedTrips = [...trips, newTrip];
      setTrips(updatedTrips);
      localStorage.setItem('trips', JSON.stringify(updatedTrips));
      
      handleClose();
    } catch (error) {
      console.error('Error saving trip:', error);
      // 这里可以添加错误提示
    }
  };

  const handleChange = (event) => {
    const { name, value } = event.target;
    setTripData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const handlePhotoUpload = (event) => {
    const files = Array.from(event.target.files);
    
    files.forEach(file => {
      const reader = new FileReader();
      
      reader.onload = () => {
        // 添加预览URL
        setPreviewUrls(prev => [...prev, reader.result]);
        
        // 读取EXIF信息
        EXIF.getData(file, function() {
          const exifData = EXIF.getAllTags(this);
          console.log('EXIF data:', exifData);
          
          if (exifData && exifData.DateTimeOriginal) {
            const dateStr = exifData.DateTimeOriginal;
            const date = new Date(
              dateStr.substring(0,4),
              parseInt(dateStr.substring(5,7)) - 1,
              dateStr.substring(8,10)
            );
            
            setTripData(prev => ({
              ...prev,
              startDate: date.toISOString().split('T')[0],
              photos: [...prev.photos, {
                file: file,
                exifData: exifData
              }]
            }));
          }

          // 如果有GPS信息，尝试获取位置
          if (exifData.GPSLatitude && exifData.GPSLongitude) {
            const lat = convertDMSToDD(exifData.GPSLatitude, exifData.GPSLatitudeRef);
            const lng = convertDMSToDD(exifData.GPSLongitude, exifData.GPSLongitudeRef);
            
            // 使用反向地理编码获取位置名称
            fetch(`https://nominatim.openstreetmap.org/reverse?format=json&lat=${lat}&lon=${lng}`)
              .then(response => response.json())
              .then(data => {
                if (data.display_name) {
                  setTripData(prev => ({
                    ...prev,
                    destination: data.display_name.split(',')[0]
                  }));
                }
              })
              .catch(error => console.error('Error fetching location:', error));
          }
        });
      };
      
      reader.readAsDataURL(file);
    });
  };

  // 将度分秒格式转换为十进制度数
  const convertDMSToDD = (dms, ref) => {
    const degrees = dms[0];
    const minutes = dms[1];
    const seconds = dms[2];
    
    let dd = degrees + minutes/60 + seconds/3600;
    if (ref === "S" || ref === "W") {
      dd = dd * -1;
    }
    return dd;
  };

  const formatDate = (dateString) => {
    if (!dateString) return '';
    const date = new Date(dateString);
    return date.toLocaleDateString('zh-CN', {
      year: 'numeric',
      month: 'long',
      day: 'numeric'
    });
  };

  return (
    <Container maxWidth="md">
      <Box sx={{ my: 4 }}>
        <Typography variant="h4" component="h1" gutterBottom>
          SnapRoute 行程记录
        </Typography>
        
        <Stack direction="row" spacing={2} sx={{ mt: 2, mb: 4 }}>
          <Button 
            variant="contained" 
            startIcon={<AddIcon />}
            onClick={handleClickOpen}
          >
            添加行程
          </Button>
          <Button
            variant="outlined"
            startIcon={<ListAltIcon />}
            onClick={() => setTripsDialogOpen(true)}
          >
            我的行程
          </Button>
        </Stack>

        {/* 添加行程对话框 */}
        <Dialog 
          open={open} 
          onClose={handleClose}
          maxWidth="md"
          fullWidth
        >
          <DialogTitle>添加新行程</DialogTitle>
          <DialogContent>
            <Box sx={{ pt: 2 }}>
              <input
                accept="image/*"
                style={{ display: 'none' }}
                id="photo-upload"
                type="file"
                multiple
                onChange={handlePhotoUpload}
              />
              <label htmlFor="photo-upload">
                <Button
                  variant="outlined"
                  component="span"
                  startIcon={<PhotoCamera />}
                  fullWidth
                  sx={{ mb: 2 }}
                >
                  上传照片
                </Button>
              </label>

              {/* 照片预览区域 */}
              {previewUrls.length > 0 && (
                <Box sx={{ mb: 2, display: 'flex', flexWrap: 'wrap', gap: 1 }}>
                  {previewUrls.map((url, index) => (
                    <Paper
                      key={index}
                      sx={{
                        width: 100,
                        height: 100,
                        overflow: 'hidden',
                        position: 'relative'
                      }}
                    >
                      <img
                        src={url}
                        alt={`预览 ${index + 1}`}
                        style={{
                          width: '100%',
                          height: '100%',
                          objectFit: 'cover'
                        }}
                      />
                    </Paper>
                  ))}
                </Box>
              )}

              <TextField
                fullWidth
                label="目的地"
                name="destination"
                value={tripData.destination}
                onChange={handleChange}
                margin="normal"
              />
              <TextField
                fullWidth
                label="开始日期"
                name="startDate"
                type="date"
                value={tripData.startDate}
                onChange={handleChange}
                margin="normal"
                InputLabelProps={{
                  shrink: true,
                }}
              />
              <TextField
                fullWidth
                label="结束日期"
                name="endDate"
                type="date"
                value={tripData.endDate}
                onChange={handleChange}
                margin="normal"
                InputLabelProps={{
                  shrink: true,
                }}
              />
              <TextField
                fullWidth
                label="备注"
                name="notes"
                value={tripData.notes}
                onChange={handleChange}
                margin="normal"
                multiline
                rows={4}
              />
            </Box>
          </DialogContent>
          <DialogActions>
            <Button onClick={handleClose}>取消</Button>
            <Button onClick={handleSubmit} variant="contained">
              保存
            </Button>
          </DialogActions>
        </Dialog>

        {/* 我的行程对话框 */}
        <Dialog
          open={tripsDialogOpen}
          onClose={() => setTripsDialogOpen(false)}
          maxWidth="md"
          fullWidth
        >
          <DialogTitle>我的行程记录</DialogTitle>
          <DialogContent>
            <Grid container spacing={2} sx={{ mt: 1 }}>
              {trips.map((trip) => (
                <Grid item xs={12} key={trip.id}>
                  <Card>
                    <CardContent>
                      <Typography variant="h6" gutterBottom>
                        {trip.destination}
                      </Typography>
                      <Typography variant="body2" color="text.secondary">
                        {formatDate(trip.startDate)} 
                        {trip.endDate && ` - ${formatDate(trip.endDate)}`}
                      </Typography>
                      {trip.notes && (
                        <Typography variant="body2" sx={{ mt: 1 }}>
                          {trip.notes}
                        </Typography>
                      )}
                    </CardContent>
                    {trip.photos.length > 0 && (
                      <Box sx={{ p: 2 }}>
                        <Grid container spacing={1}>
                          {trip.photos.map((photo, index) => (
                            <Grid item xs={4} sm={3} md={2} key={index}>
                              <CardMedia
                                component="img"
                                image={photo.url}
                                alt={`${trip.destination} 照片 ${index + 1}`}
                                sx={{
                                  height: 100,
                                  objectFit: 'cover',
                                  borderRadius: 1
                                }}
                              />
                            </Grid>
                          ))}
                        </Grid>
                      </Box>
                    )}
                    <Divider />
                  </Card>
                </Grid>
              ))}
              {trips.length === 0 && (
                <Grid item xs={12}>
                  <Typography variant="body1" color="text.secondary" align="center">
                    还没有添加任何行程记录
                  </Typography>
                </Grid>
              )}
            </Grid>
          </DialogContent>
          <DialogActions>
            <Button onClick={() => setTripsDialogOpen(false)}>关闭</Button>
          </DialogActions>
        </Dialog>
      </Box>
    </Container>
  );
}

export default App; 