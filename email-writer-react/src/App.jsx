import React, { useState } from 'react';
import './App.css';
import { Container, Typography, TextField, Box, FormControl, InputLabel, Select, MenuItem, CircularProgress, Button } from '@mui/material';
import axios from 'axios';

function App() {
  const [emailContent, setEmailContent] = useState('');
  const [tone, setTone] = useState('');
  const [generatedReply, setGeneratedReply] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleSubmit = async () => {
    setLoading(true);
    setError('');
    setGeneratedReply('');

    try {
// Replace 48.214.246.76 with your BACKEND's External IP
const response = await axios.post('http://172.168.77.220:8081/api/email/generate', {
    emailContent,
    tone,
});
      setGeneratedReply(typeof response.data === 'string' ? response.data : JSON.stringify(response.data, null, 2));

    } catch (error) {
      setError(error.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <Container maxWidth="sm" sx={{ py: 4 }}>
      <Typography variant='h3' component='h1' gutterBottom>
        Email Reply Generator
      </Typography>
      <Box sx={{ mx: 3 }}>
        <TextField
          fullWidth
          multiline
          rows={6}
          variant='outlined'
          label="Original Email Content"
          value={emailContent}
          onChange={(e) => setEmailContent(e.target.value)}
          sx={{ mb: 2 }}
        />
        <FormControl fullWidth sx={{ mb: 2 }}>
          <InputLabel id="tone-select-label">Tone (Optional)</InputLabel>
          <Select
            labelId="tone-select-label"
            value={tone}
            label="Tone (Optional)"
            onChange={(e) => setTone(e.target.value)}
          >
            <MenuItem value="">None</MenuItem>
            <MenuItem value="Professional">Professional</MenuItem>
            <MenuItem value="Casual">Casual</MenuItem>
            <MenuItem value="Friendly">Friendly</MenuItem>
          </Select>
        </FormControl>
        <Button
          variant="contained"
          onClick={handleSubmit}
          disabled={!emailContent || loading}
        >
          {loading ? <CircularProgress size={24} /> : "Generate Reply"}
        </Button>
      </Box>
      {error && (
        <Typography color='error' sx={{ mb: 2 }}>
          {error}
        </Typography>
      )}
      {generatedReply && (
        <Box sx={{ mt: 3 }}>
          <Typography variant='h5' gutterBottom></Typography>
          <TextField
            multiline
            rows={6}
            variant='outlined'
            fullWidth
            value={generatedReply || ""}
            inputProps={{ readonly: true }}
          />
          <Button
            variant='outlined'
            sx={{ mt: 2 }}
            onClick={() => {
              navigator.clipboard.writeText(generatedReply);
            }}
          >
            Copy to Clipboard
          </Button>
        </Box>
      )}
    </Container>
  );
}

export default App;
