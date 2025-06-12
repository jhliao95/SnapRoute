import React, { useState } from 'react';
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
  IconButton,
  Paper
} from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import PhotoCamera from '@mui/icons-material/PhotoCamera';
import EXIF from 'exif-js';

function App() {
  const [open, setOpen] = useState(false);
  const [tripData, setTripData] = useState({
    destination: '',
    startDate: '',
    endDate: '',
    notes: '',
    photos: []
  });
  const [previewUrls, setPreviewUrls] = useState([]);

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

  const handleSubmit = () => {
    console.log('提交的行程数据:', tripData);
    handleClose();
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

  return (
    <Container maxWidth="md">
      <Box sx={{ my: 4 }}>
        <Typography variant="h4" component="h1" gutterBottom>
          SnapRoute 行程记录
        </Typography>
        
        <Button 
          variant="contained" 
          startIcon={<AddIcon />}
          onClick={handleClickOpen}
          sx={{ mt: 2 }}
        >
          添加行程
        </Button>

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
      </Box>
    </Container>
  );
}

export default App; 